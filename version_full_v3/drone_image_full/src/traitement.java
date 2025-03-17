/**
 *-
 * Nom du fichier : traitement.java
 * Auteur         : BEAL JULIEN
 * Version        : 2.0
 * Date           : 11/02/2025
 * Description    : Classe traitement pour gérer les images reçues et envoyées
 *-
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.Enumeration;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import thread.*;
import util.*;

public class traitement {
    static {
        try {
            System.loadLibrary("JNIFileMappingPictureClient");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la librairie JNIFileMappingPictureClient");
            e.printStackTrace();
        }
        try {
            System.loadLibrary("JNIFileMappingPictureServeur");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la librairie JNIFileMappingPictureServeur");
            e.printStackTrace();
        }
        try {
            System.loadLibrary("JNIVirtualPicture");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la librairie JNIVirtualPicture");
            e.printStackTrace();
        }
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la librairie OpenCV");
            e.printStackTrace();
        }
    }

    // Déclarations des objets FileMapping
    private cFileMappingPictureClient monCLientFMP = new cFileMappingPictureClient(false);
    private cFileMappingPictureServeur monServeurFMP = new cFileMappingPictureServeur(true);

    // Variables liées aux images et au traitement
    private byte[] imageBytes;
    private int length;
    private Mat imageRecu = new Mat(), imageEnvoyer = new Mat();
    private Mat imageAfficher_source = new Mat(), imageAfficher_envoyer = new Mat();
    private BufferedImage bufferedImage_source = null, bufferedImage_envoyer = null;
    private double fps = 0.0;
    private Process process = null;
    private String address_local_str, text;

    // Configuration réseau et UDP
    private int[] port = {55000, 55001, 55002};
    private String address_broadcast = "172.29.255.255";
    private byte[] data = new byte[65536];
    private DatagramSocket socket_image;
    private DatagramSocket socket_cmd;
    private DatagramPacket packet;
    private InetAddress localAddress;

    // Paramètres d'image
    private int[] imgsize = {1280, 720};
    private int maxPacketSize = 65000; // 65536 - 8 (overhead UDP)

    // Variables pour la gestion des images OpenCV
    private Mat dermiereImageValide = new Mat();
    private Mat dermiereImageValide_resizedImage = new Mat();
    private Mat imageEnvoyer_resizedImage = new Mat();
    private Mat blackImage;
    private boolean firstImageReceived = false;
    private Size displayFrameHalfSize = new Size(0, 0);

    // Mesure du temps pour le calcul des FPS
    private long previousTime = System.nanoTime();

    // Variables de synchronisation des traitements
    private LocalDateTime Client_Time = LocalDateTime.now();
    private int Client_traitement = 0;
    private LocalDateTime droneTime, traitementTime;
    // 0 : pas de traitement, 1 : contours, 2 : formes, 3 : contours et formes
    private int traitements = 0;

    // Threads
    private thread_reception_image reception;
    private thread_reception_string commande;
    private thread_detection_contours detection_contours;
    private thread_detection_formes detection_formes;
    private thread_envoie_cmd envoie_cmd;
    private thread_list_dynamic_ip list_dynamic_ip;

    // Interfaces graphiques
    private FenetreTraitement droneFenetre;
    private FenetreTraitement fenetreTraitement;

    //--------------------------------------------------------------//
    public traitement() {

        //--------------------------------------------------------------//
        // Ouverture du serveur de FileMapping pour l'image
        try {
            this.monServeurFMP.OpenServer("img_java_to_c");
        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture du serveur img_java_to_c");
            e.printStackTrace();
        }
        //--------------------------------------------------------------//
        // Lancement du programme Chai3D
        try {
            ProcessBuilder pb = new ProcessBuilder("F:\\BEAL_JULIEN_SN2\\_projet_2025\\git\\Chai3d-3.2.0\\bin\\win-x64\\00-drone-ju.exe");
            this.process = pb.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (this.process.isAlive()) {
                    this.process.destroy();
                }
            }));
        } catch (IOException e) {
            System.out.println("Erreur lors du lancement du programme Chai3D");
            e.printStackTrace();
        }
        //--------------------------------------------------------------//
        // Ouverture du client de FileMapping pour l'image
        try {
            this.monCLientFMP.OpenClient("img_java_to_c");
            //this.monCLientFMP.OpenClient("img_c_to_java");
        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture du client img_c_to_java");
            e.printStackTrace();
        }
        //--------------------------------------------------------------//
        // Initialisation des adresses IP et des sockets UDP
        try {
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
                            this.localAddress = inetAddress;
                            this.address_local_str = ip;
                            break;
                        }
                    }
                }
                if (this.localAddress != null) {
                    break;
                }
            }
            if (this.localAddress == null) {
                throw new Exception("Aucune adresse IP locale valide trouvée.");
            }
            // Initialisation des sockets UDP
            this.socket_image = new DatagramSocket(this.port[0]);
            this.socket_cmd = new DatagramSocket(this.port[2]);
            this.packet = new DatagramPacket(this.data, this.data.length);
        } catch (Exception e) {
            System.out.println("Erreur lors de l'initialisation des adresses IP et des sockets UDP");
            e.printStackTrace();
        }
        //--------------------------------------------------------------//
        // Initialisation de l'image noire
        try {
            // Création d'une image noire avec le texte "START"
            this.blackImage = new Mat(360, 640, CvType.CV_8UC3, new Scalar(0, 0, 0));
            Size textSize = Imgproc.getTextSize("START", Imgproc.FONT_HERSHEY_SIMPLEX, 2.0, 3, null);
            Point textOrg = new Point((this.blackImage.cols() - textSize.width) / 2,(this.blackImage.rows() + textSize.height) / 2);
            Imgproc.putText(this.blackImage,"START",textOrg,Imgproc.FONT_HERSHEY_SIMPLEX,2.0,new Scalar(255, 255, 255),3);
            // Initialisation de displayFrameHalfSize (sera mis à jour dès la première image reçue)
            this.displayFrameHalfSize = new Size(0, 0);
        } catch (Exception e) {
            System.out.println("Erreur lors de l'initialisation de l'image noire");
            e.printStackTrace();
        }
        //--------------------------------------------------------------//
        // Lancement des threads
        try {
            this.reception = new thread_reception_image("traitement_UDP_image", this.socket_image, this.imageRecu);
            this.reception.start();
    
            this.commande = new thread_reception_string("traitement_UDP_String", this.socket_cmd);
            this.commande.start();
    
            this.detection_contours = new thread_detection_contours(this.imageRecu, false);
            this.detection_contours.start();
    
            this.detection_formes = new thread_detection_formes(this.imageRecu, false);
            this.detection_formes.start();
    
            this.envoie_cmd = new thread_envoie_cmd("T", this.address_local_str, this.address_broadcast, this.port[2]);
            this.envoie_cmd.start();
    
            this.list_dynamic_ip = new thread_list_dynamic_ip("traitement - boucle de vérification de la liste d'addresses");
            this.list_dynamic_ip.start();
        } catch (Exception e) {
            System.out.println("Erreur lors du lancement des threads");
            e.printStackTrace();
        }
        //--------------------------------------------------------------//
        // Initialisation de l'interface graphique
        try {
            ImageIcon icon = new ImageIcon("lib/logo.png");
            this.droneFenetre = new FenetreTraitement("drone", icon, 0, 0);
            // Renommé pour éviter le conflit avec le nom de la classe
            this.fenetreTraitement = new FenetreTraitement("traitement", icon, 640, 0);
        } catch (Exception e) {
            System.out.println("Erreur lors de l'initialisation de l'interface graphique");
            e.printStackTrace();
        }
        //--------------------------------------------------------------//
        // Ajout du shutdown hook pour supprimer l'adresse de la liste
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    this.text = "T#remove?address#" + this.address_local_str;
                    this.sendTextUDP(this.text, this.address_broadcast, this.port[2]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch (Exception e) {
            System.out.println("Erreur lors de l'ajout du shutdown hook");
            e.printStackTrace();
        }
        //--------------------------------------------------------------//
        // Boucle principale de traitement
        try {
            error.printError();
            this.mainLoop();
        } catch (Exception e) {
            System.out.println("Erreur lors de la boucle principale de traitement");
            e.printStackTrace();
        }
    }

    //--------------------------------------------------------------//
    // Boucle principale de traitement
    private void mainLoop() {
        String messageRecu;
        String[] parts;
        String action = "";
        int quality = 70;
        byte[] encodedData;
        long currentTime;
        double intervalInSeconds;

        while (true) {
            this.imageRecu = this.reception.getImageRecu();
            if (!this.imageRecu.empty()) {
                if (!this.firstImageReceived) {
                    this.displayFrameHalfSize = new Size(this.imageRecu.width() / 2, this.imageRecu.height() / 2);
                    this.firstImageReceived = true;
                }
                // Stocker la dernière image valide
                this.dermiereImageValide = this.imageRecu.clone();

                //--------------------------------------------------------------//
                // Traitement du message UDP reçu
                messageRecu = this.commande.getMessageRecu();
                if (messageRecu.startsWith("C#")) {
                    parts = messageRecu.split("\\?");
                    action = parts[0].split("#")[1];
                    if (action.equals("add")) {
                        this.list_dynamic_ip.addClient(parts[1].split("#")[1], parts[2].split("#")[1]);
                    } else if (action.equals("remove")) {
                        this.list_dynamic_ip.removeClient(parts[1].split("#")[1]);
                    } else if (action.equals("cmd")) {
                        this.list_dynamic_ip.updateClient(parts[1].split("#")[1]);
                        this.Client_Time = LocalDateTime.parse(parts[2].split("#")[1]);
                        this.Client_traitement = Integer.parseInt(parts[3].split("#")[1]);
                    }
                }
                // Réinitialisation
                messageRecu = "";
                parts = null;

                //--------------------------------------------------------------//
                // Comparaison des temps de modification pour déterminer le type de traitement
                this.droneTime = this.droneFenetre.getLastModifiedTime();
                this.traitementTime = this.fenetreTraitement.getLastModifiedTime();
                if (this.Client_Time.isAfter(this.droneTime) && this.Client_Time.isAfter(this.traitementTime)) {
                    this.traitements = this.Client_traitement;
                } else if (this.droneTime.isAfter(this.traitementTime)) {
                    this.traitements = this.droneFenetre.getTraitement();
                } else {
                    this.traitements = this.fenetreTraitement.getTraitement();
                }

                //--------------------------------------------------------------//
                // Application du traitement selon la valeur déterminée
                switch (this.traitements) {
                    case 0:
                        this.imageEnvoyer = this.imageRecu;
                        break;
                    case 1:
                        this.detection_contours.setFrame(this.imageRecu);
                        while (this.detection_contours.isFrame_process()) {
                            new tempo(1);
                        }
                        this.imageEnvoyer = this.detection_contours.getFrame();
                        break;
                    case 2:
                        this.detection_formes.setFrame(this.imageRecu);
                        while (this.detection_formes.isFrame_process()) {
                            new tempo(1);
                        }
                        this.imageEnvoyer = this.detection_formes.getFrame();
                        break;
                    case 3:
                        this.detection_contours.setFrame(this.imageRecu);
                        this.detection_formes.setFrame(this.imageRecu);
                        while (this.detection_formes.isFrame_process() || this.detection_contours.isFrame_process()) {
                            new tempo(1);
                        }
                        this.imageEnvoyer = this.additionDesDifferences(
                                this.detection_contours.getFrame(),
                                this.detection_formes.getFrame(),
                                this.imageRecu);
                        break;
                }

                //--------------------------------------------------------------//
                // Calcul des FPS
                currentTime = System.nanoTime();
                intervalInSeconds = (currentTime - this.previousTime) / 1_000_000_000.0;
                this.fps = 1.0 / intervalInSeconds;
                Imgproc.putText(this.imageEnvoyer, String.format("FPS: %.0f", this.fps),
                        new Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX, 1,
                        new Scalar(0, 255, 0), 2);
                this.previousTime = currentTime;

                //--------------------------------------------------------------//
                // Redimensionner les images pour l'affichage
                Imgproc.resize(this.dermiereImageValide, this.dermiereImageValide_resizedImage, this.displayFrameHalfSize);
                Imgproc.resize(this.imageEnvoyer, this.imageEnvoyer_resizedImage, this.displayFrameHalfSize);
                if (this.firstImageReceived) {
                    this.imageAfficher_source = this.dermiereImageValide_resizedImage;
                    this.imageAfficher_envoyer = this.imageEnvoyer_resizedImage;
                } else {
                    this.imageAfficher_source = this.blackImage;
                    this.imageAfficher_envoyer = this.blackImage;
                }

                //--------------------------------------------------------------//
                // Mise à jour du FileMapping pour Chai3D
                this.imageBytes = this.encodeImageToJPEG(this.imageEnvoyer, 100);
                this.length = this.imageBytes.length;
                for (int i = 0; i < this.length; i++) {
                    this.monServeurFMP.setMapFileOneByOneUnsignedChar(i, this.imageBytes[i]);
                }
                for (int i = 0; i < this.length; i++) {
                    this.imageBytes[i] = (byte) this.monCLientFMP.getMapFileOneByOneUnsignedChar(i);
                }

                //--------------------------------------------------------------//
                // Ajustement dynamique du taux de compression
                quality = 70;
                if (this.imageBytes.length > this.maxPacketSize) {
                    this.imageEnvoyer = jpegToMat(this.imageBytes);
                    do {
                        encodedData = this.encodeImageToJPEG(this.imageEnvoyer, quality);
                        quality -= 5;
                    } while (encodedData.length > this.maxPacketSize && quality > 10);
                } else {
                    encodedData = this.imageBytes;
                }

                //--------------------------------------------------------------//
                // Envoi de l'image UDP à chaque adresse de la liste
                if (!this.list_dynamic_ip.getClientAddress().isEmpty()) {
                    for (String addr : this.list_dynamic_ip.getClientAddress()) {
                        try {
                            this.sendImageUDP(encodedData, addr, this.port[1]);
                        } catch (IOException e) {
                            System.out.println("Erreur lors de l'envoi de l'image à " + addr + " : " + e.getMessage());
                        }
                    }
                }
            } else {
                // Aucun image reçue : afficher l'image noire ou la dernière image valide
                if (this.dermiereImageValide != null) {
                    if (!this.firstImageReceived) {
                        this.imageAfficher_source = this.blackImage;
                        this.imageAfficher_envoyer = this.blackImage;
                    } else {
                        this.imageAfficher_source = this.dermiereImageValide_resizedImage;
                        this.imageAfficher_envoyer = this.imageEnvoyer_resizedImage;
                    }
                } else {
                    this.imageAfficher_source = this.blackImage;
                    this.imageAfficher_envoyer = this.blackImage;
                }
            }
            //--------------------------------------------------------------//
            // Mise à jour des fenêtres d'affichage
            try {
                this.bufferedImage_source = this.byteArrayToBufferedImage(this.encodeImageToJPEG(this.imageAfficher_source, 100));
                this.droneFenetre.setImage(this.bufferedImage_source);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (this.imageBytes != null) {
                    this.bufferedImage_envoyer = this.byteArrayToBufferedImage(resizeJPEGImage(this.imageBytes, this.displayFrameHalfSize));
                } else {
                    this.bufferedImage_envoyer = this.byteArrayToBufferedImage(this.encodeImageToJPEG(this.imageAfficher_envoyer, 100));
                }
                this.fenetreTraitement.setImage(this.bufferedImage_envoyer);
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
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de l'image à " + address + " : " + e.getMessage());
        }finally {
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
            socket = new DatagramSocket();
            InetAddress ipAddress = InetAddress.getByName(address);
            byte[] buffer = data.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipAddress, port);
            socket.send(packet);
            System.out.println("Données envoyées à " + address + ":" + port);
            System.out.println("Données envoyées : " + data);
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de l'image à " + address + " : " + e.getMessage());
        }finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
    //--------------------------------------------------------------//
    // Méthode pour encoder une image en JPEG avec une qualité donnée
    private byte[] encodeImageToJPEG(Mat image, int quality) {
        MatOfByte matOfByte = new MatOfByte();
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
        if (img1.size().equals(img2.size()) && img1.type() == img2.type() &&
            img1.size().equals(source.size()) && img1.type() == source.type()) {
            Mat diff = new Mat();
            Core.absdiff(img1, img2, diff);
            Mat result = source.clone();
            Core.addWeighted(source, 1.0, diff, 1.0, 0.0, result);
            return result;
        } else {
            throw new IllegalArgumentException("Les dimensions ou types des images ne correspondent pas");
        }
    }
    //--------------------------------------------------------------//
    // Méthode pour redimensionner une image JPEG
    public static byte[] resizeJPEGImage(byte[] jpegImageBytes, Size newSize) {
        MatOfByte mob = new MatOfByte(jpegImageBytes);
        Mat image = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
        if (image.empty()) {
            System.err.println("Erreur lors du décodage de l'image JPEG.");
            return null;
        }
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, newSize);
        MatOfByte mobResized = new MatOfByte();
        Imgcodecs.imencode(".jpg", resizedImage, mobResized);
        return mobResized.toArray();
    }
    //--------------------------------------------------------------//
    // Méthode pour convertir un tableau de bytes JPEG en Mat
    public static Mat jpegToMat(byte[] jpegBytes) {
        MatOfByte mob = new MatOfByte(jpegBytes);
        Mat image = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
        return image;
    }
}
