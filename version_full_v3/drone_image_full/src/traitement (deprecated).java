/*
*-------------------------------------------------------------------
* Nom du fichier : traitement.java
* Auteur         : BEAL JULIEN
* Version        : 3.1
* Date           : 11/02/2025
* Description    : Classe traitement pour gérer les images reçues et envoyées
*-------------------------------------------------------------------
* © 2025 BEAL JULIEN - Tous droits réservés
*/


/*

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
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
            System.err.println("\nErreur lors du chargement de la librairie JNIFileMappingPictureClient");
            e.printStackTrace();
        }
        try {
            System.loadLibrary("JNIFileMappingPictureServeur");
        } catch (Exception e) {
            System.err.println("\nErreur lors du chargement de la librairie JNIFileMappingPictureServeur");
            e.printStackTrace();
        }
        try {
            System.loadLibrary("JNIVirtualPicture");
        } catch (Exception e) {
            System.err.println("\nErreur lors du chargement de la librairie JNIVirtualPicture");
            e.printStackTrace();
        }
        try {
            System.loadLibrary("JNIFileMappingDroneCharTelemetryServeur");
        } catch (Exception e) {
            System.err.println("\nErreur lors du chargement de la librairie JNIFileMappingDroneCharTelemetryServeur");
            e.printStackTrace();
        }
        try {
            System.loadLibrary("JNIVirtualDroneCharTelemetry");
        } catch (Exception e) {
            System.err.println("\nErreur lors du chargement de la librairie JNIVirtualDroneCharTelemetry");
            e.printStackTrace();
        }
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (Exception e) {
            System.err.println("\nErreur lors du chargement de la librairie OpenCV");
            e.printStackTrace();
        }
    }

    // Déclarations des objets FileMapping
    private cFileMappingPictureClient client_filemap_image = new cFileMappingPictureClient(false);
    private cFileMappingPictureServeur serveur_filemap_image = new cFileMappingPictureServeur(false);

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
    private int[] port = { 55000, 55001, 55002, 55003 };
    private String address_broadcast = "172.29.255.255";
    private byte[] data = new byte[65536];
    private DatagramSocket socket_image;
    private DatagramSocket socket_cmd;
    private DatagramSocket socketTelemetrie;
    private DatagramPacket packet;
    private InetAddress localAddress;

    // Paramètres d'image
    private int[] imgsize = { 1280, 720 };
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
    private thread_traitement_telemtrie telemetrie_traitement;

    private thread_detection_contours detection_contours;
    private thread_detection_formes detection_formes;
    private thread_envoie_cmd envoie_cmd;
    private thread_list_dynamic_ip list_dynamic_ip;

    // Interfaces graphiques
    private FenetreTraitement droneFenetre;
    private FenetreTraitement fenetreTraitement;

    // Variables
    private String commandeRecu;
    private String telemetryRecu;
    private String[] parts;
    private String action = "";
    private int quality = 70;
    private byte[] encodedData;
    private long currentTime;
    private double intervalInSeconds;

    // --------------------------------------------------------------//
    public traitement() {

        // --------------------------------------------------------------//
        // Ouverture du serveur de FileMapping pour l'image
        try {
            this.serveur_filemap_image.OpenServer("img_java_to_c");
            
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'ouverture du serveur img_java_to_c");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // Create a new thread for Chai3D process
        Thread chai3dThread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "cmd.exe", "/c", "start", "\"Chai3D Console\"",
                        "\"F:\\BEAL_JULIEN_SN2\\_projet_2025\\git\\@Chai3d-3.2.0_VisualStudio_2015_x64-VirtualDevice-Formes-08\\bin\\win-x64\\00-drone-ju.exe\"");
                this.process = pb.start();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (this.process.isAlive()) {
                        this.process.destroy();
                    }
                }));
            } catch (IOException e) {
                System.out.println("\nErreur lors du lancement du programme Chai3D");
                e.printStackTrace();
            }
        });
        chai3dThread.start();
        new tempo(2000); // attendre le démarrage
        // --------------------------------------------------------------//
        // Ouverture du client de FileMapping pour l'image
        try {
            // this.client_filemap_image.OpenClient("img_java_to_c");
            this.client_filemap_image.OpenClient("img_c_to_java");
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'ouverture du client img_c_to_java");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
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
            this.socketTelemetrie = new DatagramSocket(this.port[3]);
            this.packet = new DatagramPacket(this.data, this.data.length);
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'initialisation des adresses IP et des sockets UDP");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // Initialisation de l'image noire
        try {
            // Création d'une image noire avec le texte "START"
            this.blackImage = new Mat(360, 640, CvType.CV_8UC3, new Scalar(0, 0, 0));
            Size textSize = Imgproc.getTextSize("No Image", Imgproc.FONT_HERSHEY_SIMPLEX, 2.0, 3, null);
            Point textOrg = new Point((this.blackImage.cols() - textSize.width) / 2,
                    (this.blackImage.rows() + textSize.height) / 2);
            Imgproc.putText(this.blackImage, "No Image", textOrg, Imgproc.FONT_HERSHEY_SIMPLEX, 2.0,
                    new Scalar(255, 255, 255), 3);
            // Initialisation de displayFrameHalfSize (sera mis à jour dès la première image
            // reçue)
            this.displayFrameHalfSize = new Size(0, 0);
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'initialisation de l'image noire");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        try {
            while (this.serveur_filemap_image.getVirtualPictureMutexBlocAccess()) {
                new tempo(1);
            }
            this.serveur_filemap_image.setVirtualPictureMutexBlocAccess(true);
            this.imageBytes = this.encodeImageToJPEG(this.blackImage, 100);
            this.length = this.imageBytes.length;
            for (int i = 0; i < this.length; i++) {
                this.serveur_filemap_image.setMapFileOneByOneUnsignedChar(i, this.imageBytes[i]);
            }
            this.serveur_filemap_image.setVirtualPictureDataSize(this.imageBytes.length);
            this.serveur_filemap_image.setVirtualPictureMutexBlocAccess(false);
        }catch (Exception e) {
            System.out.println("\nErreur lors de l'initialisation de l'image noire dans le FileMapping");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // Lancement des threads avec gestion individuelle des exceptions
        try {
            this.reception = new thread_reception_image("traitement_UDP_image", this.socket_image, this.imageRecu);
            this.reception.start();
        } catch (Exception e) {
            System.out.println("\nErreur lors du lancement du thread de réception d'image");
            e.printStackTrace();
        }
        try {
            this.commande = new thread_reception_string("reception_cmd_traitement", this.socket_cmd);
            this.commande.start();
        } catch (Exception e) {
            System.out.println("\nErreur lors du lancement du thread de réception de commande");
            e.printStackTrace();
        }
        try {
            this.telemetrie_traitement = new thread_traitement_telemtrie("reception_Telemetrie", this.socketTelemetrie);
            this.telemetrie_traitement.start();
        } catch (Exception e) {
            System.out.println("\nErreur lors du lancement du thread de réception télémétrie");
            e.printStackTrace();
        }
        try {
            this.detection_contours = new thread_detection_contours(this.imageRecu, false);
            this.detection_contours.start();
        } catch (Exception e) {
            System.out.println("\nErreur lors du lancement du thread de détection des contours");
            e.printStackTrace();
        }
        try {
            this.detection_formes = new thread_detection_formes(this.imageRecu, false);
            this.detection_formes.start();
        } catch (Exception e) {
            System.out.println("\nErreur lors du lancement du thread de détection des formes");
            e.printStackTrace();
        }
        try {
            this.envoie_cmd = new thread_envoie_cmd("T", this.address_local_str, this.address_broadcast, this.port[2]);
            this.envoie_cmd.start();
        } catch (Exception e) {
            System.out.println("\nErreur lors du lancement du thread d'envoi de commande");
            e.printStackTrace();
        }
        try {
            this.list_dynamic_ip = new thread_list_dynamic_ip(
                    "traitement - boucle de vérification de la liste d'adresses");
            this.list_dynamic_ip.start();
        } catch (Exception e) {
            System.out.println("\nErreur lors du lancement du thread de gestion des IP dynamiques");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // Initialisation de l'interface graphique
        try {
            ImageIcon icon = new ImageIcon("lib/logo.png");
            this.droneFenetre = new FenetreTraitement("drone", icon, 0, 0);
            // Renommé pour éviter le conflit avec le nom de la classe
            this.fenetreTraitement = new FenetreTraitement("traitement", icon, 640, 0);
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'initialisation de l'interface graphique");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // Ajout du shutdown hook pour supprimer l'adresse de la liste et tuer Chai3D
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    // Remove address from list
                    this.text = "T#remove?address#" + this.address_local_str;
                    this.sendTextUDP(this.text, this.address_broadcast, this.port[2]);

                    Runtime.getRuntime().exec("taskkill /F 00-drone-ju.exe");

                } catch (IOException e) {
                    System.err.println("Error in shutdown hook: " + e.getMessage());
                    e.printStackTrace();
                }
            }));
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'ajout du shutdown hook");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // Boucle principale de traitement
        try {
            error.printError();
            this.mainLoop();
        } catch (Exception e) {
            System.out.println("\nErreur lors de la boucle principale de traitement");
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------//
    // Boucle principale de traitement
    private void mainLoop() {

        while (true) {
            this.imageRecu = this.reception.getImageRecu();

            // System.out.println(this.imageRecu);
            if (!this.imageRecu.empty()) {
                if (!this.firstImageReceived) {
                    this.displayFrameHalfSize = new Size(this.imageRecu.width() / 2, this.imageRecu.height() / 2);
                    this.firstImageReceived = true;
                }
                // Stocker la dernière image valide
                this.dermiereImageValide = this.imageRecu.clone();

                // --------------------------------------------------------------//
                // Traitement du message UDP reçu
                this.parts = null;
                this.commandeRecu = this.commande.getMessageRecu();
                if (!this.commandeRecu.isEmpty()) {
                    if (this.commandeRecu.startsWith("C#")) {
                        this.parts = this.commandeRecu.split("\\?");
                        this.action = this.parts[0].split("#")[1];
                        if (this.action.equals("add")) {
                            this.list_dynamic_ip.addClient(this.parts[1].split("#")[1], this.parts[2].split("#")[1]);
                        } else if (this.action.equals("remove")) {
                            this.list_dynamic_ip.removeClient(this.parts[1].split("#")[1]);
                        } else if (this.action.equals("cmd")) {
                            this.list_dynamic_ip.updateClient(this.parts[1].split("#")[1]);
                            this.Client_Time = LocalDateTime.parse(this.parts[2].split("#")[1]);
                            this.Client_traitement = Integer.parseInt(this.parts[3].split("#")[1]);
                        }
                    }
                }
                // Réinitialisation
                this.commandeRecu = "";
                this.parts = null;

                // --------------------------------------------------------------//
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

                // --------------------------------------------------------------//
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

                // --------------------------------------------------------------//
                // Calcul des FPS
                currentTime = System.nanoTime();
                intervalInSeconds = (currentTime - this.previousTime) / 1_000_000_000.0;
                this.fps = 1.0 / intervalInSeconds;
                Imgproc.putText(this.imageEnvoyer, String.format("FPS: %.0f", this.fps),
                        new Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX, 1,
                        new Scalar(0, 255, 0), 2);
                System.out.print("\033[K"); // Efface la ligne
                System.out.print(String.format("\rfps: %.0f   ", fps));
                this.previousTime = currentTime;

                // --------------------------------------------------------------//
                // Redimensionner les images pour l'affichage
                Imgproc.resize(this.dermiereImageValide, this.dermiereImageValide_resizedImage,
                        this.displayFrameHalfSize);
                Imgproc.resize(this.imageEnvoyer, this.imageEnvoyer_resizedImage, this.displayFrameHalfSize);
                if (this.firstImageReceived) {
                    this.imageAfficher_source = this.dermiereImageValide_resizedImage;
                    this.imageAfficher_envoyer = this.imageEnvoyer_resizedImage;
                } else {
                    this.imageAfficher_source = this.blackImage;
                    this.imageAfficher_envoyer = this.blackImage;
                }
                // --------------------------------------------------------------//
                // Mise à jour du FileMapping pour Chai3D
                // On test pour savoir si le client ne lit pas le FMP
                while (this.serveur_filemap_image.getVirtualPictureMutexBlocAccess()) {
                    new tempo(1);
                }
                this.serveur_filemap_image.setVirtualPictureMutexBlocAccess(true);
                this.imageBytes = this.encodeImageToJPEG(this.imageEnvoyer, 100);
                if (this.imageBytes != null) {
                    this.length = this.imageBytes.length;
                    for (int i = 0; i < this.length; i++) {
                        this.serveur_filemap_image.setMapFileOneByOneUnsignedChar(i, this.imageBytes[i]);
                    }
                    this.serveur_filemap_image.setVirtualPictureDataSize(this.imageBytes.length);
                } else {
                    // Use black image as fallback
                    this.imageBytes = this.encodeImageToJPEG(this.blackImage, 100);
                    if (this.imageBytes != null) {
                        this.length = this.imageBytes.length;
                        for (int i = 0; i < this.length; i++) {
                            this.serveur_filemap_image.setMapFileOneByOneUnsignedChar(i, this.imageBytes[i]);
                        }
                        this.serveur_filemap_image.setVirtualPictureDataSize(this.imageBytes.length);
                    }
                }
                this.serveur_filemap_image.setVirtualPictureMutexBlocAccess(false);

                // --------------------------------------------------------------//
                // Mise à jour du FileMapping pour le traitement Java
                
                while (this.client_filemap_image.getVirtualPictureMutexBlocAccess()) {
                    new tempo(1);
                }
                this.client_filemap_image.setVirtualPictureMutexBlocAccess(true);
                this.length = this.client_filemap_image.getVirtualPictureDataSize();
                this.imageBytes = null;
                for (int i = 0; i < this.length; i++) {
                    this.imageBytes[i] = (byte) this.client_filemap_image.getMapFileOneByOneUnsignedChar(i);
                }
                this.client_filemap_image.setVirtualPictureMutexBlocAccess(false);
                // --------------------------------------------------------------//
                // Ajustement dynamique du taux de compression
                this.quality = 70;
                if (this.imageBytes != null && this.imageBytes.length > 0) {
                    if (this.imageBytes.length > this.maxPacketSize) {
                        Mat tempMat = jpegToMat(this.imageBytes);
                        if (tempMat != null && !tempMat.empty()) {
                            this.imageEnvoyer = tempMat;
                            do {
                                this.encodedData = this.encodeImageToJPEG(this.imageEnvoyer, this.quality);
                                this.quality -= 5;
                            } while (this.encodedData != null && this.encodedData.length > this.maxPacketSize && this.quality > 10);
                        } else {
                            // Fallback to using existing image data
                            this.encodedData = this.imageBytes;
                        }
                    } else {
                        this.encodedData = this.imageBytes;
                    }
                } else {
                    // Use black image as fallback if imageBytes is null or empty
                    this.encodedData = this.encodeImageToJPEG(this.blackImage, this.quality);
                }

                // Envoi de l'image UDP à chaque adresse de la liste
                if (!this.list_dynamic_ip.getClientAddress().isEmpty() && this.encodedData != null) {
                    for (String addr : this.list_dynamic_ip.getClientAddress()) {
                        try {
                            this.sendImageUDP(this.encodedData, addr, this.port[1]);
                        } catch (IOException e) {
                            System.err.println("Erreur lors de l'envoi de l'image à " + addr + ": " + e.getMessage());
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
            // --------------------------------------------------------------//
            // Mise à jour des fenêtres d'affichage
            try {
                byte[] encodedImage = this.encodeImageToJPEG(this.imageAfficher_source, 100);
                if (encodedImage != null) {
                    this.bufferedImage_source = this.byteArrayToBufferedImage(encodedImage);
                    if (this.bufferedImage_source != null) {
                        this.droneFenetre.setImage(this.bufferedImage_source);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error updating source display: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                if (this.imageBytes != null && this.imageBytes.length > 0) {
                    // Convert imageBytes to Mat first
                    Mat tempMat = jpegToMat(this.imageBytes);
                    if (tempMat != null && !tempMat.empty()) {
                        // Resize the Mat image
                        Mat resizedMat = new Mat();
                        Imgproc.resize(tempMat, resizedMat, this.displayFrameHalfSize);
                        
                        // Convert resized Mat back to JPEG bytes
                        byte[] resizedImageBytes = this.encodeImageToJPEG(resizedMat, 100);
                        if (resizedImageBytes != null) {
                            this.bufferedImage_envoyer = this.byteArrayToBufferedImage(resizedImageBytes);
                            if (this.bufferedImage_envoyer != null) {
                                this.fenetreTraitement.setImage(this.bufferedImage_envoyer);
                            }
                        }
                    }
                } else {
                    // Fallback to black image if imageBytes is null or empty
                    byte[] encodedImage = this.encodeImageToJPEG(this.blackImage, 100);
                    if (encodedImage != null) {
                        this.bufferedImage_envoyer = this.byteArrayToBufferedImage(encodedImage);
                        this.fenetreTraitement.setImage(this.bufferedImage_envoyer);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error updating processed display: " + e.getMessage());
                e.printStackTrace();
            }
            new tempo(1);
        }
    }

    // --------------------------------------------------------------//
    // Méthode pour envoyer une image via UDP
    private void sendImageUDP(byte[] imageData, String address, int port) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress ipAddress = InetAddress.getByName(address);
            DatagramPacket packet = new DatagramPacket(imageData, imageData.length, ipAddress, port);
            socket.send(packet);
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'envoi de l'image à " + address + " : " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    // --------------------------------------------------------------//
    // Méthode pour envoyer un String via UDP
    private void sendTextUDP(String data, String address, int port) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress ipAddress = InetAddress.getByName(address);
            byte[] buffer = data.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipAddress, port);
            socket.send(packet);
            // System.out.println("\nDonnées envoyées à " + address + ":" + port);
            // System.out.println("\nDonnées envoyées : " + data);
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'envoi de l'image à " + address + " : " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    // --------------------------------------------------------------//
    // Méthode pour encoder une image en JPEG avec une qualité donnée
    private byte[] encodeImageToJPEG(Mat image, int quality) {
        if (image == null || image.empty()) {
            System.err.println("Warning: Attempt to encode empty or null image");
            return null;
        }

        try {
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", image, matOfByte, new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality));
            return matOfByte.toArray();
        } catch (CvException e) {
            System.err.println("Error encoding image: " + e.getMessage());
            return null;
        }
    }

    // --------------------------------------------------------------//
    // Méthode pour convertir un tableau d'octets en BufferedImage
    private BufferedImage byteArrayToBufferedImage(byte[] byteArray) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        return ImageIO.read(bis);
    }

    // --------------------------------------------------------------//
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

    // --------------------------------------------------------------//
    // Méthode pour redimensionner une image JPEG
    public byte[] resizeJPEGImage(byte[] jpegImageBytes, Size newSize) {
        MatOfByte mob = new MatOfByte(jpegImageBytes);
        Mat image = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
        if (image.empty()) {
            System.err.println("\nErreur lors du décodage de l'image JPEG.");
            return null;
        }
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, newSize);
        MatOfByte mobResized = new MatOfByte();
        Imgcodecs.imencode(".jpg", resizedImage, mobResized);
        return mobResized.toArray();
    }

    // --------------------------------------------------------------//
    // Méthode pour convertir un tableau de bytes JPEG en Mat
    public Mat jpegToMat(byte[] jpegBytes) {
        MatOfByte mob = new MatOfByte(jpegBytes);
        Mat image = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
        return image;
    }
}
*/