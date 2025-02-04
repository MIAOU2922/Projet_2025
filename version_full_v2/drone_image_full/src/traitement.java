/**
 * -------------------------------------------------------------------
 * Nom du fichier : traitement.java
 * Auteur         : BEAL JULIEN
 * Version        : 1.0
 * Date           : 03/02/2025
 * Description    : class traitement
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class traitement {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Charger la bibliothèque OpenCV
    }

    Mat imageRecu = new Mat() , imageEnvoyer = new Mat() , imageAfficher_source = new Mat() ,  imageAfficher_envoyer = new Mat();
    BufferedImage bufferedImage_source = null, bufferedImage_envoyer = null;


    public traitement () {

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
    
            // Initialisation du socket UD
            socket = new DatagramSocket(port[0]);
            packet = new DatagramPacket(data, data.length);
        } catch (Exception e) {
            
        }
        
        // Définition de la taille de l'image
        int imgsize[] = {1280, 720};
    
        // Initialisation des matrices OpenCV
        Mat dermiereImageValide = new Mat(),
            dermiereImageValide_resizedImage = new Mat(),
            imageEnvoyer_resizedImage = new Mat();


        
        long currentTime , previousTime =System.nanoTime() ;
        double intervalInSeconds , fps;

        int quality = 70; // Qualité initiale
        byte[] encodedData;

        // Taille maximale autorisée pour un paquet UDP (en bytes)
        int maxPacketSize = 65528; // 65536 - 8 (overhead UDP)

        //type de traitement
        int traitements = 0;
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
            new org.opencv.core.Point((blackImage.cols() - Imgproc.getTextSize("START", Imgproc.FONT_HERSHEY_SIMPLEX, 2.0, 3, null).width) / 2, 
                      (blackImage.rows() + Imgproc.getTextSize("START", Imgproc.FONT_HERSHEY_SIMPLEX, 2.0, 3, null).height) / 2), 
            Imgproc.FONT_HERSHEY_SIMPLEX,
            2.0,
            new Scalar(255, 255, 255),
            3
        );
        
        boolean firstImageReceived = false , firstTraitement = false; // Indicateur pour savoir si la première image est reçue

        thread_reception reception = new thread_reception(socket, imageRecu);
        reception.start();

        thread_detection_contours detection_contours = new thread_detection_contours(imageRecu, false);
        detection_contours.start();

        thread_detection_formes detection_formes = new thread_detection_formes(imageRecu, false);
        detection_formes.start();

        int loop = 0;

//--------------------------------------------------------------//
         // Charger l'icône depuis les ressources
        ImageIcon icon = new ImageIcon("lib/logo.png");
        
         // Création de la fenêtre client pour afficher l'image reçue
        JFrame frame_char = new JFrame("Char");
        frame_char.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame_char.setSize(640, 500);
        frame_char.setLayout(new BorderLayout());
        frame_char.setResizable(false);
        frame_char.setIconImage(icon.getImage());

        JPanel panel_char = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bufferedImage_source != null) {
                    g.drawImage(bufferedImage_source, 0, 0, null);
                }
            }
        };
        frame_char.add(panel_char, BorderLayout.CENTER);
        frame_char.setVisible(true);

        // Création de la fenêtre client pour afficher l'image traiter

        // Création de la fenêtre client pour afficher l'image reçue
        JFrame frame_traitement = new JFrame("traitement");
        frame_traitement.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame_traitement.setSize(640, 500);
        frame_traitement.setLayout(new BorderLayout());
        frame_traitement.setResizable(false);
        frame_traitement.setIconImage(icon.getImage());
        frame_traitement.setLocation(640,0);

        JPanel panel_traitement = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bufferedImage_envoyer != null) {
                    g.drawImage(bufferedImage_envoyer, 0, 0, null);
                }
            }
        };
        frame_traitement.add(panel_traitement, BorderLayout.CENTER);
        frame_traitement.setVisible(true);

        //--------------------------------------------------------------//
        error.printError();
        // Boucle principale
        while (true) {

            this.imageRecu = reception.getImageRecu();

            if (!this.imageRecu.empty()) {
                
                firstImageReceived = true; // Indiquer que la première image est reçue
                System.out.println("Image reçue");
                
                dermiereImageValide = this.imageRecu.clone(); // Stocker l'image d'origine comme dernière valide
                
                traitements = 3;
                /*
                0 : pas de triatement
                1 : traitement contour
                2 : traitement forme
                3 : traitement contour et forme
                */
                switch(traitements){
                    case 0:
                        imageEnvoyer = this.imageRecu;
                    break;
                    case 1:
                        detection_contours.setFrame(this.imageRecu);
                        while ( detection_contours.isFrame_process() != false){
                            new tempo(1);
                        }
                        imageEnvoyer = detection_contours.getFrame();
                    break;
                    case 2:
                        detection_formes.setFrame(this.imageRecu);
                        while ( detection_formes.isFrame_process() != false){
                            new tempo(1);
                        }
                        imageEnvoyer = detection_formes.getFrame();
                    break;
                    case 3:
                        detection_contours.setFrame(this.imageRecu);
                        detection_formes.setFrame(this.imageRecu);
                        while (detection_formes.isFrame_process() || detection_contours.isFrame_process()) { 
                            new tempo(1);
                        }
                        imageEnvoyer = additionDesDifferences(detection_contours.getFrame(),detection_formes.getFrame(),this.imageRecu);
                    break;
                }
                firstTraitement = true;
                // Calculer les FPS
                currentTime = System.nanoTime();
                intervalInSeconds = (currentTime - previousTime) / 1_000_000_000.0; // Intervalle en secondes
                fps = 1.0 / intervalInSeconds; // Calcul des FPS
                System.out.printf(" FPS: %.0f\n", fps);

                Imgproc.putText(imageEnvoyer, String.format("FPS: %.0f", fps), new org.opencv.core.Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX,1, new Scalar(0, 255, 0), 2);

                // Mettre à jour le temps précédent
                previousTime = currentTime;

                // Réduire la taille de l'image avant de l'afficher
                Size displayFrameHalfSize = new Size(imageEnvoyer.width() / 2, imageEnvoyer.height() / 2);

                Imgproc.resize(dermiereImageValide, dermiereImageValide_resizedImage, displayFrameHalfSize);
                Imgproc.resize(imageEnvoyer, imageEnvoyer_resizedImage, displayFrameHalfSize);

                 // Afficher soit la première image reçue, soit l'image noire
                if (firstImageReceived) {
                    imageAfficher_source = dermiereImageValide_resizedImage;
                    imageAfficher_envoyer = imageEnvoyer_resizedImage;
                } else {
                    imageAfficher_source = blackImage ;
                    imageAfficher_envoyer = blackImage ;
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
                    sendImageUDP(encodedData, address, port[1]);
                    System.out.printf(" FPS: %.0f\n", fps);


                } catch (IOException e) {
                    System.out.println("Erreur lors de l'envoi de l'image : " + e.getMessage());
                }

            } else {

                //System.out.println("Image non reçue");
                if (dermiereImageValide != null) {
                    if (firstImageReceived == false) {
                        // Afficher l'image noire si aucune image n'est reçue
                        imageAfficher_source = blackImage ;
                        imageAfficher_envoyer = blackImage ;
                    } else {
                        // Afficher la dernière image valide
                        imageAfficher_source = dermiereImageValide_resizedImage;
                        imageAfficher_envoyer = imageEnvoyer_resizedImage;
                    }
                }
            }

            try {
                bufferedImage_source = byteArrayToBufferedImage(encodeImageToJPEG(imageAfficher_source, 100));
                SwingUtilities.invokeLater(() -> panel_char.repaint());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                bufferedImage_envoyer = byteArrayToBufferedImage(encodeImageToJPEG(imageAfficher_envoyer, 100));
                SwingUtilities.invokeLater(() -> panel_traitement.repaint());
            } catch (IOException e) {
                e.printStackTrace();
            }
            new tempo(5);
        }
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

    //--------------------------------------------------------------//
    // Méthode pour convertir un tableau d'octets en BufferedImage
    private BufferedImage byteArrayToBufferedImage(byte[] byteArray) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        return ImageIO.read(bis);
    }

    //--------------------------------------------------------------//
    //
    public Mat additionDesDifferences(Mat img1, Mat img2, Mat source) {
        // Vérification des tailles et types d'images
        if (img1.size().equals(img2.size()) && img1.type() == img2.type() && img1.size().equals(source.size()) && img1.type() == source.type()) {
            
            // Calculer la différence absolue entre img1 et img2
            Mat diff = new Mat();
            Core.absdiff(img1, img2, diff);
            
            // Appliquer la différence sur l'image source
            Mat result = source.clone();
            Core.addWeighted(source, 1.0, diff, 1.0, 0.0, result);
            
            return result;  // L'image résultante avec les différences appliquées sur la source
        } else {
            throw new IllegalArgumentException("Les dimensions ou types des images ne correspondent pas");
        }
    }
}
