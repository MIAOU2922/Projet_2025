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

package main;

import java.awt.image.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import thread.thread_detection_contours;
import thread.thread_detection_formes;
import thread.thread_reception_image;
import thread.thread_reception_string;
import util.error;
import util.tempo;
import util.FenetreTraitement;

public class traitement {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Charger la bibliothèque OpenCV
    }
    

    Mat imageRecu = new Mat() , imageEnvoyer = new Mat() , imageAfficher_source = new Mat() ,  imageAfficher_envoyer = new Mat();
    BufferedImage bufferedImage_source = null, bufferedImage_envoyer = null;
    double fps = 0.0;
    private ArrayList <String> client_address = new ArrayList<>();
    private ArrayList <String> client_time = new ArrayList<>();

    public traitement () {

        // Définition des ports UDP
        int port[] = {
            55000, // Port de réception traitement
            55001, // Port de réception client
            55002 // Port de commande image
            };
        
        // Définition des adresses IP
        List<String> address = new ArrayList<>();

        String address_broadcast = "172.29.255.255";


        String messageRecu ;
        String[] parts;
        LocalDateTime Client_Time = LocalDateTime.now();
        int Client_traitement = 0;

        byte[] data = new byte[65536];
        
        DatagramSocket socket_image = null;
        DatagramSocket socket_cmd = null;
        DatagramPacket packet = null;

        String address_local_str = "";
        InetAddress address_local = null;

        try {
            // Obtenir l'adresse IP locale
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress instanceof Inet4Address) {
                        String ip = inetAddress.getHostAddress();
                        if (ip.startsWith("172.29.41.")) {
                            address_local = inetAddress;
                            address_local_str = ip;
                            break;
                        }
                    }
                }
                if (address_local != null) {
                    break;
                }
            }

            if (address_local == null) {
                throw new Exception("Aucune adresse IP locale valide trouvée.");
            }

            // Initialisation du socket UDP
            socket_image = new DatagramSocket(port[0]);
            socket_cmd = new DatagramSocket(port[2]);
            packet = new DatagramPacket(data, data.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        // Définition de la taille de l'image
        int imgsize[] = {1280, 720};
    
        // Initialisation des matrices OpenCV
        Mat dermiereImageValide = new Mat(),
            dermiereImageValide_resizedImage = new Mat(),
            imageEnvoyer_resizedImage = new Mat();


        
        long currentTime , previousTime =System.nanoTime() ;
        double intervalInSeconds ;

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
        
        boolean firstImageReceived = false ; // Indicateur pour savoir si la première image est reçue

        thread_reception_image reception = new thread_reception_image("traitement_UDP_image",socket_image, imageRecu);
        reception.start();

        thread_reception_string commande = new thread_reception_string("traitement_UDP_String", socket_cmd);
        commande.start();

        thread_detection_contours detection_contours = new thread_detection_contours(imageRecu, false);
        detection_contours.start();

        thread_detection_formes detection_formes = new thread_detection_formes(imageRecu, false);
        detection_formes.start();

        // Thread pour vérifier les adresses toutes les minutes
        new Thread(() -> {
            Thread.currentThread().setName("boucle d'afk");
            while (true) {
                try {
                    LocalDateTime now = LocalDateTime.now();
                    for (int i = 0; i < client_time.size(); i++) {
                        LocalDateTime clientTime = LocalDateTime.parse(client_time.get(i));
                        if (ChronoUnit.MINUTES.between(clientTime, now) > 3) {
                            System.out.println("Adresse " + client_address.get(i) + " supprimée pour inactivité.");
                            client_address.remove(i);
                            client_time.remove(i);
                            i--; // Ajuster l'index après la suppression
                        }
                    }
                    System.out.println("Liste des adresses : " + client_address + " (" + client_address.size() + ")" + client_time + " (" + client_time.size() + ")");
                    Thread.sleep(1000); // Vérification toutes les minutes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();

        //--------------------------------------------------------------//
         // Charger l'icône depuis les ressources
        ImageIcon icon = new ImageIcon("lib/logo.png");

        FenetreTraitement drone = new FenetreTraitement("drone", icon, 0, 0);
        FenetreTraitement traitement = new FenetreTraitement("traitement", icon, 640, 0);
        
        //--------------------------------------------------------------//
        //--------------------------------------------------------------//
        //--------------------------- boucle ---------------------------//
        //--------------------------------------------------------------//
        //--------------------------------------------------------------//
        error.printError();
        // Boucle principale
        while (true) {

            this.imageRecu = reception.getImageRecu();

            if (!this.imageRecu.empty()) {
                
                firstImageReceived = true; // Indiquer que la première image est reçue
                //System.out.println("Image reçue");
                
                dermiereImageValide = this.imageRecu.clone(); // Stocker l'image d'origine comme dernière valide
                
                messageRecu = commande.getMessageRecu();
                
                parts = messageRecu.split("\\?"); // Split using "?"
                for (String part : parts) {
                    if (part.startsWith("traitement#")) {
                        Client_traitement = Integer.parseInt(part.split("#")[1]);
                    } else if (part.startsWith("time#")) {
                        String timeString = part.split("#")[1];
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                        Client_Time = LocalDateTime.parse(timeString, formatter);
                    } else if (part.startsWith("address#")) {
                        String clientAddressPort = part.split("#")[1];
                        String[] addressPortParts = clientAddressPort.split(":");
                        String clientAddress = addressPortParts[0];
                        
                        // Vérifier si l'adresse est déjà présente dans client_address
                        int index = client_address.indexOf(clientAddress);
                        if (index == -1) {
                            // Si l'adresse n'est pas présente, l'ajouter
                            client_address.add(clientAddress);
                            client_time.add(Client_Time.toString());
                        } else {
                            // Si l'adresse est déjà présente, mettre à jour le temps
                            client_time.set(index, Client_Time.toString());
                        }
                    }
                }

                // Comparer les temps de modification et mettre à jour la valeur de traitement
                LocalDateTime droneTime = drone.getLastModifiedTime();
                LocalDateTime traitementTime = traitement.getLastModifiedTime();

                if (Client_Time.isAfter(droneTime) && Client_Time.isAfter(traitementTime)) {
                    traitements = Client_traitement;
                } else if (droneTime.isAfter(traitementTime)) {
                    traitements = drone.getTraitement();
                } else {
                    traitements = traitement.getTraitement();
                }

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
                // Calculer les FPS
                currentTime = System.nanoTime();
                intervalInSeconds = (currentTime - previousTime) / 1_000_000_000.0; // Intervalle en secondes
                fps = 1.0 / intervalInSeconds; // Calcul des FPS
                //System.out.printf(" FPS: %.0f\n", fps);

                Imgproc.putText(imageEnvoyer, String.format(" FPS: %.0f", fps), new org.opencv.core.Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX,1, new Scalar(0, 255, 0), 2);

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

                //--------------------------------------------------------------//
                // chai3D





                //--------------------------------------------------------------//


                
                // Ajuster dynamiquement le taux de compression
                quality = 70; // Qualité initiale
                // Encoder l'image en JPEG et ajuster la qualité si nécessaire
                do {
                    encodedData = encodeImageToJPEG(imageEnvoyer, quality);
                    quality -= 5; // Réduire la qualité de compression
                } while (encodedData.length > maxPacketSize && quality > 10); // Réduire jusqu'à ce que l'image tienne dans un paquet UDP
                // Envoi de l'image à chaque adresse dans la liste
                if (!client_address.isEmpty()) {
                    for (String addr : client_address) {
                        try {
                            sendImageUDP(encodedData, addr, port[1]);
                        } catch (IOException e) {
                            System.out.println("Erreur lors de l'envoi de l'image à " + addr + " : " + e.getMessage());
                        }
                    }
                } else {
                    //System.out.println("La liste des adresses est vide, aucune image n'a été envoyée.");
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
                drone.setImage(bufferedImage_source);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                bufferedImage_envoyer = byteArrayToBufferedImage(encodeImageToJPEG(imageAfficher_envoyer, 100));
                traitement.setImage(bufferedImage_envoyer);
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
            //System.out.println("Image envoyée à " + address + ":" + port + String.format(" FPS: %.0f", fps));
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    //--------------------------------------------------------------//
    // Méthode pour envoyer un String via UDP
    private void sendTextUDP(String data, String address, int port) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(); // Crée un socket UDP
            InetAddress ipAddress = InetAddress.getByName(address); // Résolution de l'adresse IP
            
            byte[] buffer = data.getBytes(); // Convertir le texte en tableau d'octets
            
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipAddress, port);
            socket.send(packet); // Envoie du paquet UDP
            
            //System.out.println("Données envoyées à " + address + ":" + port + " FPS: "+ fps);
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Ferme le socket proprement
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
