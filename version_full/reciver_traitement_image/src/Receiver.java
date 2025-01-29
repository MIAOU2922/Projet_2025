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
        DatagramSocket socket = new DatagramSocket(port[1]);
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
                
                // Réduire la taille de l'image avant de l'afficher
                Size displayFrameHalfSize = new Size(imageRecu.width() / 2, imageRecu.height() / 2);
                Imgproc.resize(dermiereImageValide, dermiereImageValide_resizedImage, displayFrameHalfSize);
                HighGui.imshow("source", dermiereImageValide_resizedImage); // Afficher l'image redimensionnée

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
}
