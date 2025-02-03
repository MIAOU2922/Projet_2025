import java.net.*;
import java.util.*;
import java.io.*;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class reciver {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Charger la bibliothèque OpenCV
    }

    Mat imageRecu = new Mat();

    public reciver () {
        
        // Définition des ports UDP
        int port[] = {
            55000, // Port de réception traitement
            55001, // Port de réception client
            55002 // Port de commande image
            };
        
        // Définition des adresses IP
        String address = "172.29.41.9";
        String address_broadcast = "172.29.255.255";
        
        byte[] data = new byte[65536];

        DatagramSocket socket = null;
        DatagramPacket packet = null;

        try {
            // Obtenir l'adresse IP locale
            InetAddress address_local = InetAddress.getLocalHost();
            String address_local_str = address_local.getHostAddress();

            // Initialisation du socket UDP
            socket = new DatagramSocket(port[1]);
            packet = new DatagramPacket(data, data.length);

        } catch (Exception e) {
            
        }

        // Définition de la taille de l'image
        int imgsize[] = {1280, 720};
        // Initialisation des matrices OpenCV
        Mat processedImage , processedImage2 , dermiereImageValide = null;

        Mat resizedImage = new Mat() , resizedImage2 = new Mat() , dermiereImageValide_resizedImage = new Mat();

        long currentTime , previousTime =System.nanoTime() ;
        double intervalInSeconds , fps;

        Mat blackImage = new Mat(360, 640, CvType.CV_8UC3, new Scalar(0, 0, 0));

        Imgproc.putText(
            blackImage, 
            "START", 
            new Point((blackImage.cols() - Imgproc.getTextSize("START", Imgproc.FONT_HERSHEY_SIMPLEX, 2.0, 3, null).width) / 2, 
                      (blackImage.rows() + Imgproc.getTextSize("START", Imgproc.FONT_HERSHEY_SIMPLEX, 2.0, 3, null).height) / 2), 
            Imgproc.FONT_HERSHEY_SIMPLEX, 
            2.0, 
            new Scalar(255, 255, 255), 
            3
        );
        
        boolean firstImageReceived = false; // Indicateur pour savoir si la première image est reçue

        thread_reception reception = new thread_reception(socket, imageRecu);
        reception.start();

        //--------------------------------------------------------------//
        // Boucle principale
        while (true) {
            
            this.imageRecu = reception.getImageRecu();


            if (this.imageRecu.empty()) {
                System.out.println("Image non reçue");
                if (dermiereImageValide != null) {
                    // Afficher la dernière image valide
                    HighGui.imshow("client", dermiereImageValide);
                }else{
                    // Afficher une image noire si aucune image n'est reçue
                    HighGui.imshow("client", blackImage);
                }
            } else {
                //System.out.printf("Image reçue");
                dermiereImageValide = this.imageRecu.clone(); // Stocker l'image d'origine comme dernière valide
                
                // Calculer les FPS
                currentTime = System.nanoTime();
                intervalInSeconds = (currentTime - previousTime) / 1_000_000_000.0; // Intervalle en secondes
                fps = 1.0 / intervalInSeconds; // Calcul des FPS
                //System.out.printf(" FPS: %.0f\n", fps);

                Imgproc.putText(dermiereImageValide, String.format("FPS: %.0f", fps), new Point(10, 60), Imgproc.FONT_HERSHEY_SIMPLEX,1, new Scalar(255, 0,0 ), 2);

                // Mettre à jour le temps précédent
                previousTime = currentTime;
                // Réduire la taille de l'image avant de l'afficher
                Size displayFrameHalfSize = new Size(this.imageRecu.width() / 2, this.imageRecu.height() / 2);
                Imgproc.resize(dermiereImageValide, dermiereImageValide_resizedImage, displayFrameHalfSize);
                HighGui.imshow("client", dermiereImageValide_resizedImage); // Afficher l'image redimensionnée

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
