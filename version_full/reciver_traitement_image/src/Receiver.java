import java.net.*;
import java.io.*;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Receiver {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Charger la bibliothèque OpenCV
    }
    public static void main(String[] args) throws Exception {
        // Définition des ports et adresses IP
        int port[] = {4000, 4001, 4002, 4003, 4004, 4005, 4006, 4007, 4008, 4009};
        String address = "172.29.41.9";
        String address_broadcast = "172.29.255.255";
        // Obtenir l'adresse IP locale
        InetAddress address_local = InetAddress.getLocalHost();
        String address_local_str = address_local.getHostAddress();
        // Définition de la taille de l'image
        int imgsize[] = {1280, 720};
        // Initialisation des matrices
        Mat imageRecu = new Mat();
        Mat processedImage , dermiereImageValide = null;
        byte[] data = new byte[65536];
        // Initialisation du socket UDP
        DatagramSocket socket = new DatagramSocket(port[0]);
        DatagramPacket packet = new DatagramPacket(data, data.length);
        //--------------------------------------------------------------//
        // Boucle principale
        while (true) {
            // Réception de l'image via UDP
            socket.receive(packet);
            // Convertir les données reçues en une image
            imageRecu = Imgcodecs.imdecode(new MatOfByte(packet.getData()), Imgcodecs.IMREAD_COLOR);
            if (imageRecu.empty()) {
                System.out.println("Image non reçue");
                if (dermiereImageValide != null) {
                    // Afficher la dernière image valide
                    HighGui.imshow("Contour", dermiereImageValide);
                }
            } else {
                System.out.println("Image reçue");
                dermiereImageValide = imageRecu.clone(); // Stocker l'image d'origine comme dernière valide
                processedImage = processImage(imageRecu); // Traiter l'image pour les contours
                
                // Réduire la taille de l'image avant de l'afficher
                Mat resizedImage = new Mat();
                Size displayFrameHalfSize = new Size(processedImage.width() / 2, processedImage.height() / 2);
                Imgproc.resize(processedImage, resizedImage, displayFrameHalfSize);
                
                HighGui.imshow("Contour", resizedImage); // Afficher l'image redimensionnée
            }
            //tempo
            int key = HighGui.waitKey(10);
            if (key == 27) {
                break;
            }
        }
        // Fermer toutes les fenêtres après la boucle
        HighGui.destroyAllWindows();
    }
    //--------------------------------------------------------------//
    // Méthode pour traiter l'image
    private static Mat processImage(Mat image) {
        // Convertir l'image en niveaux de gris
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        // Détection des contours avec l'algorithme Canny
        Mat edges = new Mat();
        int lowerThreshold = 10;
        int upperThreshold = 40;
        Imgproc.Canny(grayImage, edges, lowerThreshold, upperThreshold);
        // Créer une image avec des contours rouges
        Mat edgesRed = Mat.zeros(edges.size(), image.type()); // Initialiser une image noire
        // Appliquer la dilatation pour augmenter la taille des contours
        Mat dilatedEdges = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)); // Structure de la dilatation (taille de 3x3)
        Imgproc.dilate(edges, dilatedEdges, kernel); // Dilater les contours
        // Remplir les contours dilatés avec la couleur rouge
        for (int row = 0; row < dilatedEdges.rows(); row++) {
            for (int col = 0; col < dilatedEdges.cols(); col++) {
                if (dilatedEdges.get(row, col)[0] > 0) {
                    // Définir la couleur rouge pour les pixels correspondant aux contours
                    edgesRed.put(row, col, new double[]{0, 0, 255});  // Couleur rouge (BGR)
                }
            }
        }
        // Superposer les contours rouges sur l'image d'origine sans flou
        Mat combinedImage = new Mat();
        image.copyTo(combinedImage); // Copie de l'image d'origine dans l'image combinée
        Core.add(edgesRed, combinedImage, combinedImage); // Ajouter les contours rouges dilatés
        return combinedImage; // Retourner l'image combinée
    }

}
