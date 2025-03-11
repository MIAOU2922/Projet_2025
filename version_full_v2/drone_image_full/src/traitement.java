/**
 * -------------------------------------------------------------------
 * Nom du fichier : traitement.java
 * Auteur         : BEAL JULIEN
 * Version        : 2.0
 * Date           : 11/02/2025
 * Description    : Classe traitement pour gérer les images reçues et envoyées
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

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
        try {
            System.loadLibrary("JNIFileMappingPictureClient");
        }catch (Exception e) {
            System.err.println("Erreur lors du chargement de la librairie JNIFileMappingPictureClient");
            e.printStackTrace();
        }
        try {
            System.loadLibrary("JNIFileMappingPictureServeur");
        }catch (Exception e) {
            System.err.println("Erreur lors du chargement de la librairie JNIFileMappingPictureServeur");
            e.printStackTrace();
        }
        try {
            System.loadLibrary("JNIVirtualPicture");
        }catch (Exception e) {
            System.err.println("Erreur lors du chargement de la librairie JNIVirtualPicture");
            e.printStackTrace();
        }
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        }catch (Exception e) {
            System.err.println("Erreur lors du chargement de la librairie OpenCV");
            e.printStackTrace();
        }
    }

    private cFileMappingPictureClient monCLientFMP = new cFileMappingPictureClient(false);
    private cFileMappingPictureServeur monServeurFMP = new cFileMappingPictureServeur(true);


    //--------------------------------------------------------------//


    byte[] imageBytes;
    int length;


    Mat imageRecu = new Mat() , imageEnvoyer = new Mat() , imageAfficher_source = new Mat() ,  imageAfficher_envoyer = new Mat();
    BufferedImage bufferedImage_source = null, bufferedImage_envoyer = null;
    double fps = 0.0;

    Process process = null;
    String address_local_str ;

    private ArrayList <String> client_address = new ArrayList<>();
    private ArrayList <String> client_time = new ArrayList<>();

    public traitement () {

        // ouvrir le serveur de filemapping
        monServeurFMP.OpenServer("img_java_to_c");

        // lancer chai3d
        
        try {
            ProcessBuilder pb = new ProcessBuilder("F:\\BEAL_JULIEN_SN2\\_projet_2025\\git\\Chai3d-3.2.0\\bin\\win-x64\\00-drone-ju.exe");
            process = pb.start();
            // Ajout d'un hook d'arrêt pour détruire le processus à la fin du programme Java
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (process.isAlive()) {
                    process.destroy();
                }
            }));
            // Votre code continue ici sans être bloqué
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // ouvrir le client de filemapping 
        try {
            //boucle pour juste se servir du filemap a mettre en com une fois  chai3d mis en place 
            monCLientFMP.OpenClient("img_java_to_c");
            
            //une fois chel3d mis 
            //monCLientFMP.OpenClient("img_c_to_java");
        }catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture du client img_c_to_java");
            e.printStackTrace();
        }

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
        LocalDateTime Client_Time = LocalDateTime.now() , update_afk = LocalDateTime.now();
        int Client_traitement = 0;

        byte[] data = new byte[65536];
        
        DatagramSocket socket_image = null;
        DatagramSocket socket_cmd = null;
        DatagramPacket packet = null;

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
                        if (ip.startsWith("172.29")) {
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


        
        long currentTime , previousTime =System.nanoTime();
        double intervalInSeconds ;

        int quality = 70; // Qualité initiale
        byte[] encodedData;

        // Taille maximale autorisée pour un paquet UDP (en bytes)
        int maxPacketSize = 65000; // 65536 - 8 (overhead UDP)

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


        // Réduire la taille de l'image avant de l'afficher
        Size displayFrameHalfSize = new Size(imageRecu.width() / 2, imageRecu.height() / 2);

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
                    Thread.sleep(10000); // Vérification toutes les 10 secondes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();

        // Thread pour envoyer l'adresse IP locale toutes les 1 minute et 30 secondes
        new Thread(() -> {
            Thread.currentThread().setName("boucle d'afk");
            while (true) {
                try {
                    sendTextUDP("traitement#"+getLastTwoSegments(address_local_str)+"?address#" + address_local_str + "?time#" + LocalDateTime.now(), address_broadcast, port[2]);
                    Thread.sleep(90000); // attendre 1 minute et 30 secondes
                } catch (Exception e) {
                    e.printStackTrace();
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
                
                if(firstImageReceived == false){
                    displayFrameHalfSize = new Size(imageRecu.width() / 2, imageRecu.height() / 2);
                }

                firstImageReceived = true; // Indiquer que la première image est reçue
                //System.out.println("Image reçue");

                dermiereImageValide = this.imageRecu.clone(); // Stocker l'image d'origine comme dernière valide
                
                // Traitement du message reçu
                messageRecu = commande.getMessageRecu();

                if (messageRecu.startsWith("client#")) {
                    parts = messageRecu.split("\\?");
                    boolean isTraitement = false;
                    
                    // Parcourir chaque partie de la trame
                    for (String part : parts) {
                        if (part.startsWith("client#")) {
                            // Pour la partie "client", on peut éventuellement extraire des infos spécifiques
                            // ou simplement l'ignorer, car elle sert de signal pour lancer le traitement.
                        } else if (part.startsWith("traitement#")) {
                            Client_traitement = Integer.parseInt(part.split("#")[1]);
                            if (Client_traitement == 0 || Client_traitement == 1 || Client_traitement == 2 || Client_traitement == 3) {
                                isTraitement = true;
                            }
                        } else if (part.startsWith("time#")) {
                            String timeString = part.split("#")[1];
                            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                            if (isTraitement) {
                                Client_Time = LocalDateTime.parse(timeString, formatter);
                            }
                            update_afk = LocalDateTime.parse(timeString, formatter);
                        } else if (part.startsWith("address#")) {
                            String clientAddressPort = part.split("#")[1];
                            String[] addressPortParts = clientAddressPort.split(":");
                            String clientAddress = addressPortParts[0];
                            int index = client_address.indexOf(clientAddress);
                            if (index == -1) {
                                client_address.add(clientAddress);
                                client_time.add(update_afk.toString());
                            } else {
                                client_time.set(index, update_afk.toString());
                            }
                        }
                    }
                    // Réinitialisation facultative
                    messageRecu = "";
                    parts = null;
                } else {
                    // Le message ne commence pas par "client#" : aucun traitement n'est effectué.
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
                1 : traitement contours
                2 : traitement formes
                3 : traitement contours et formes
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

                Imgproc.putText(imageEnvoyer, String.format("FPS: %.0f", fps), new org.opencv.core.Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);

                // Mettre à jour le temps précédent
                previousTime = currentTime;

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

                imageBytes = encodeImageToJPEG(this.imageEnvoyer, 100); // encodeImageToJPEG est une méthode déjà présent
                length = imageBytes.length;
                // Écrire chaque octet dans la map file via le serveur FMP
                for (int i = 0; i < length; i++) {
                    monServeurFMP.setMapFileOneByOneUnsignedChar(i, imageBytes[i]);
                }

                for (int i = 0; i < length; i++) {
                    imageBytes[i] = (byte) monCLientFMP.getMapFileOneByOneUnsignedChar(i);
            
                }

                //--------------------------------------------------------------//
                
                // Ajuster dynamiquement le taux de compression
                quality = 70; // Qualité initiale

                if (imageBytes.length > maxPacketSize) {
                    imageEnvoyer = jpegToMat(imageBytes); // Convertir le tableau de bytes en Mat
                    
                    // Encoder l'image en JPEG et ajuster la qualité si nécessaire
                    do {
                        encodedData = encodeImageToJPEG(imageEnvoyer, quality);
                        quality -= 5; // Réduire la qualité de compression
                    } while (encodedData.length > maxPacketSize && quality > 10); // Réduire jusqu'à ce que l'image tienne dans un paquet UDP

                }else{
                    encodedData = imageBytes;
                }

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
                }else{
                    // Afficher l'image noire si aucune image n'est reçue
                    imageAfficher_source = blackImage ;
                    imageAfficher_envoyer = blackImage ;
                }
            }

            try {
                bufferedImage_source = byteArrayToBufferedImage(encodeImageToJPEG(imageAfficher_source, 100));
                drone.setImage(bufferedImage_source);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                // avant chai3d
                //bufferedImage_envoyer = byteArrayToBufferedImage(encodeImageToJPEG(imageAfficher_envoyer, 100));

                // image provenan du filemap

                // ajouté une verification si le imageBytes est null
                if (imageBytes != null) {
                    bufferedImage_envoyer = byteArrayToBufferedImage(resizeJPEGImage(imageBytes, displayFrameHalfSize));
                } else {
                    bufferedImage_envoyer = byteArrayToBufferedImage(encodeImageToJPEG(imageAfficher_envoyer, 100));
                }
                
                traitement.setImage(bufferedImage_envoyer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            new tempo(1);
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
            System.out.println("Image envoyée à " + address + ":" + port );
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
            
            System.out.println("Données envoyées à " + address + ":" + port );
            System.out.println("Données envoyées : " + data);

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
    // Méthode pour ajouter les différences entre deux images sur une image source
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

    //--------------------------------------------------------------//
    // Méthode pour redimensionner une image JPEG
    public static byte[] resizeJPEGImage(byte[] jpegImageBytes, Size newSize) {
        // Convertir le tableau de bytes en Mat
        MatOfByte mob = new MatOfByte(jpegImageBytes);
        Mat image = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
        if (image.empty()) {
            System.err.println("Erreur lors du décodage de l'image JPEG.");
            return null;
        }
        
        // Redimensionner l'image
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, newSize);
        
        // Encoder l'image redimensionnée en JPEG
        MatOfByte mobResized = new MatOfByte();
        Imgcodecs.imencode(".jpg", resizedImage, mobResized);
        
        // Retourner le tableau de bytes
        return mobResized.toArray();
    }

    //--------------------------------------------------------------//
    // Méthode pour convertir un tableau de bytes en Mat
    public static Mat jpegToMat(byte[] jpegBytes) {
        // Encapsuler le tableau de bytes dans un MatOfByte
        MatOfByte mob = new MatOfByte(jpegBytes);
        // Décoder le MatOfByte pour obtenir le Mat (l'image en couleur par défaut)
        Mat image = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
        return image;
    }
     //--------------------------------------------------------------//
    // Méthode pour obtenir les deux derniers segments d'une adresse IP
    public static String getLastTwoSegments(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length >= 2) {
            return parts[parts.length - 2] + ".." + parts[parts.length - 1];
        }
        return ip;
    }
}
