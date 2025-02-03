import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class traitement {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Charger la bibliothèque OpenCV
    }

    Mat imageRecu = new Mat();

    public traitement () {
        // Définition des ports et adresses IP
        int port[] = {4000, 4001, 4002, 4003, 4004, 4005, 4006, 4007, 4008, 4009};
        String address = "172.29.41.9";
        String address_broadcast = "172.29.255.255";
        

        byte[] data = new byte[65536];
        
        DatagramSocket socket = null;

        try {
            // Obtenir l'adresse IP locale

            InetAddress address_local = InetAddress.getLocalHost();
            String address_local_str = address_local.getHostAddress();
    
            // Initialisation du socket UD
            socket = new DatagramSocket(port[0]);
        } catch (Exception e) {
            
        }
        
        // Définition de la taille de l'image
        int imgsize[] = {1280, 720};
    
        // Initialisation des matrices OpenCV
        Mat dermiereImageValide = new Mat(),
            imageEnvoyer = new Mat(),
            dermiereImageValide_resizedImage = new Mat(),
            imageEnvoyer_resizedImage = new Mat();


        
        long currentTime , previousTime =System.nanoTime() ;
        double intervalInSeconds , fps;

        int quality = 70; // Qualité initiale
        byte[] encodedData;

        // Taille maximale autorisée pour un paquet UDP (en bytes)
        int maxPacketSize = 65528; // 65536 - 8 (overhead UDP)

        //type de traitement
        int traitements = 1;
        /* 
        0 : pas de triatement 
        1 : traitement contour
        2 : traitement forme
        3 : traitement contour et forme 
        */

        // Créer une image noire avec du texte
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

            if (!this.imageRecu.empty()) {
                
                firstImageReceived = true; // Indiquer que la première image est reçue
                //System.out.printf("Image reçue");
                
                dermiereImageValide = this.imageRecu.clone(); // Stocker l'image d'origine comme dernière valide
                
                switch(traitements){
                    case 0:
                        imageEnvoyer = this.imageRecu;
                        break;
                    case 1:
                        imageEnvoyer = processImage(this.imageRecu);
                        break;
                    case 2:
                        imageEnvoyer = detectShapes(this.imageRecu);
                        break;
                    case 3:
                        imageEnvoyer = detectShapes(processImage(this.imageRecu));
                        break;
                }

                // Calculer les FPS
                currentTime = System.nanoTime();
                intervalInSeconds = (currentTime - previousTime) / 1_000_000_000.0; // Intervalle en secondes
                fps = 1.0 / intervalInSeconds; // Calcul des FPS
                //System.out.printf(" FPS: %.0f\n", fps);

                Imgproc.putText(imageEnvoyer, String.format("FPS: %.0f", fps), new Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX,1, new Scalar(0, 255, 0), 2);

                // Mettre à jour le temps précédent
                previousTime = currentTime;

                // Réduire la taille de l'image avant de l'afficher
                Size displayFrameHalfSize = new Size(imageEnvoyer.width() / 2, imageEnvoyer.height() / 2);

                Imgproc.resize(dermiereImageValide, dermiereImageValide_resizedImage, displayFrameHalfSize);
                Imgproc.resize(imageEnvoyer, imageEnvoyer_resizedImage, displayFrameHalfSize);

                 // Afficher soit la première image reçue, soit l'image noire
                if (firstImageReceived) {
                    HighGui.imshow("char", dermiereImageValide_resizedImage);
                    HighGui.imshow("traitement", imageEnvoyer_resizedImage);
                } else {
                    HighGui.imshow("char", blackImage);
                    HighGui.imshow("traitement", blackImage);
                }
                
                // Ajuster dynamiquement le taux de compression
                quality = 70; // Qualité initiale
                // Encoder l'image en JPEG et ajuster la qualité si nécessaire
                do {
                    encodedData = encodeImageToJPEG(imageEnvoyer, quality);
                    quality -= 5; // Réduire la qualité de compression
                } while (encodedData.length > maxPacketSize && quality > 10); // Réduire jusqu'à ce que l'image tienne dans un paquet UDP
                // Envoi de l'image
                try {
                    sendImageUDP(encodedData, address_broadcast, port[1]);
                    currentTime = System.nanoTime();
                    intervalInSeconds = (currentTime - previousTime) / 1_000_000_000.0; // Intervalle en secondes
                    fps = 1.0 / intervalInSeconds; // Calcul des FPS
                    System.out.printf(" FPS: %.0f\n", fps);

                    // Mettre à jour le temps précédent
                    previousTime = currentTime;

                } catch (IOException e) {
                    System.out.println("Erreur lors de l'envoi de l'image : " + e.getMessage());
                }

            } else {

                System.out.println("Image non reçue");
                if (dermiereImageValide != null) {
                    if (firstImageReceived == false) {
                        // Afficher l'image noire si aucune image n'est reçue
                        HighGui.imshow("char", blackImage);
                        HighGui.imshow("traitement", blackImage);
                    } else {
                        // Afficher la dernière image valide
                        HighGui.imshow("char", dermiereImageValide_resizedImage); // Afficher l'image redimensionnée
                        HighGui.imshow("traitement", imageEnvoyer_resizedImage); // Afficher l'image redimensionnée

                    }
                }
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
    private Mat processImage(Mat image) {
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
    private Mat detectShapes(Mat frame) {
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
//--------------------------------------------------------------//
    // Méthode pour envoyer une image via UDP
    private void sendImageUDP(byte[] imageData, String address, int port) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress ipAddress = InetAddress.getByName(address);
            DatagramPacket packet = new DatagramPacket(imageData, imageData.length, ipAddress, port);
            socket.send(packet);
            System.out.print("Image envoyée à " + address + ":" + port );
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
//--------------------------------------------------------------//
    // Méthode pour encoder une image en JPEG avec un taux de compression donné
    private byte[] encodeImageToJPEG(Mat image, int quality) {
        MatOfByte matOfByte = new MatOfByte();
        // Encoder l'image en JPEG avec un taux de compression spécifique
        Imgcodecs.imencode(".jpg", image, matOfByte, new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality));
        return matOfByte.toArray();
    }

}
