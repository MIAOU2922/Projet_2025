import java.net.*;
import java.util.ArrayList;
import java.util.List;
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
        Mat processedImage , processedImage2 , dermiereImageValide = null;
        byte[] data = new byte[65536];
        // Initialisation du socket UDP
        DatagramSocket socket = new DatagramSocket(port[0]);
        DatagramPacket packet = new DatagramPacket(data, data.length);

        Mat resizedImage = new Mat() , resizedImage2 = new Mat() , dermiereImageValide_resizedImage = new Mat();

        long currentTime , previousTime =System.nanoTime() ;
        double intervalInSeconds , fps;

        //--------------------------------------------------------------//
        // Boucle principale
        while (true) {
            // Réception de l'image via UDP
            socket.receive(packet);
            // Convertir les données reçues en une image
            imageRecu = Imgcodecs.imdecode(new MatOfByte(packet.getData()), Imgcodecs.IMREAD_COLOR);
            if (imageRecu.empty()) {
                System.out.printf("Image non reçue");
                if (dermiereImageValide != null) {
                    // Afficher la dernière image valide
                    HighGui.imshow("Contour", dermiereImageValide);
                }
            } else {
                //System.out.printf("Image reçue");
                dermiereImageValide = imageRecu.clone(); // Stocker l'image d'origine comme dernière valide
                processedImage = processImage(imageRecu); // Traiter l'image pour les contours
                processedImage2 = detectShapes(imageRecu); // Traiter l'image pour les formes

                // Calculer les FPS
                currentTime = System.nanoTime();
                intervalInSeconds = (currentTime - previousTime) / 1_000_000_000.0; // Intervalle en secondes
                fps = 1.0 / intervalInSeconds; // Calcul des FPS
                //System.out.printf(" FPS: %.0f\n", fps);

                Imgproc.putText(processedImage, String.format("FPS: %.0f", fps), new Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX,1, new Scalar(0, 255, 0), 2);
                Imgproc.putText(processedImage2, String.format("FPS: %.0f", fps), new Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX,1, new Scalar(0, 255, 0), 2);
                Imgproc.putText(dermiereImageValide, String.format("FPS: %.0f", fps), new Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX,1, new Scalar(0, 255, 0), 2);


                // Mettre à jour le temps précédent
                previousTime = currentTime;

                // Réduire la taille de l'image avant de l'afficher
                Size displayFrameHalfSize = new Size(processedImage.width() / 2, processedImage.height() / 2);
                Imgproc.resize(processedImage, resizedImage, displayFrameHalfSize);
                HighGui.imshow("Contour", resizedImage); // Afficher l'image redimensionnée
                Imgproc.resize(dermiereImageValide, dermiereImageValide_resizedImage, displayFrameHalfSize);
                HighGui.imshow("source", dermiereImageValide_resizedImage); // Afficher l'image redimensionnée
                Imgproc.resize(processedImage2, resizedImage2, displayFrameHalfSize);
                HighGui.imshow("forme", resizedImage2); // Afficher l'image redimensionnée

            }

            //tempo
            int key = HighGui.waitKey(5);
            if (key == 27) {
                break;
            }
        }
        // Fermer toutes les fenêtres après la boucle
        HighGui.destroyAllWindows();
    }
    //--------------------------------------------------------------//
    // Méthode detection contours
    private static Mat processImage(Mat image) {
        // Réutilisation des matrices pour éviter de les recréer à chaque appel
        Mat grayImage = new Mat();
        Mat flouImage = new Mat();
        Mat edges = new Mat();
        Mat dilatedEdges = new Mat();
        Mat edgesRed = Mat.zeros(image.size(), image.type()); // Initialiser une image noire
        Mat combinedImage = new Mat();
    
        // Convertir l'image en niveaux de gris
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
    
        // Appliquer un filtre bilatéral (lissage tout en préservant les contours)
        Imgproc.bilateralFilter(grayImage, flouImage, 9, 75, 75);
    
        // Détection des contours avec l'algorithme Canny
        int lowerThreshold = 0;
        int upperThreshold = 50;
        Imgproc.Canny(flouImage, edges, lowerThreshold, upperThreshold);
    
        // Appliquer la dilatation pour augmenter la taille des contours
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)); // Taille 3x3
        Imgproc.dilate(edges, dilatedEdges, kernel);
    
        // Appliquer la couleur rouge (vectorisé)
        edgesRed.setTo(new Scalar(0, 0, 255), dilatedEdges); // Affecter rouge là où les contours sont présents
    
        // Superposer les contours rouges sur l'image d'origine
        Core.add(image, edgesRed, combinedImage);
    
        // Libérer les ressources inutilisées (libère explicitement la mémoire des objets temporaires)
        grayImage.release();
        flouImage.release();
        edges.release();
        dilatedEdges.release();
        edgesRed.release();
    
        return combinedImage; // Retourner l'image combinée
    }
    
    //--------------------------------------------------------------//
    // Méthode détection forme
    private static Mat detectShapes(Mat frame) {
        Mat grayImage = new Mat();
        Mat blurredImage = new Mat();
        Mat edges = new Mat();
        Mat processedImage = frame.clone();

        // Convertir l'image en niveaux de gris
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Appliquer un flou gaussien pour réduire le bruit
        Imgproc.GaussianBlur(grayImage, blurredImage, new Size(5, 5), 0);

        // Détection des contours avec Canny
        Imgproc.Canny(blurredImage, edges, 50, 150);

        // Trouver les contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            // Approximation des contours pour simplifier la forme
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double perimeter = Imgproc.arcLength(contour2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, approx, 0.02 * perimeter, true);

            // Identifier la forme selon le nombre de sommets
            int vertexCount = approx.toArray().length;
            String shapeType = switch (vertexCount) {
                case 3 -> "Triangle";
                case 4 -> "Rectangle";
                case 5 -> "Pentagone";
                case 6 -> "Hexagone";
                case 7 -> "Heptagone";
                case 8 -> "Octogone";
                default -> " ?";
            };

            // Dessiner les contours et afficher la forme détectée
            Imgproc.drawContours(processedImage, List.of(contour), -1, new Scalar(0, 255, 0), 2);
            if (!shapeType.equals(" ")) {
                Point textPoint = approx.toArray()[0];
                Imgproc.putText(processedImage, shapeType, textPoint, Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(255, 0, 0), 2);
            }
        }

        // Libérer les ressources inutilisées
        grayImage.release();
        blurredImage.release();
        edges.release();
        hierarchy.release();

        return processedImage;
    }



}
