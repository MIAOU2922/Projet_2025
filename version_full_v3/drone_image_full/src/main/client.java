/**
 * -------------------------------------------------------------------
 * Nom du fichier : client.java
 * Auteur         : BEAL JULIEN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Code affichage client
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package main;

import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.Enumeration;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import thread.thread_reception_image;
import thread.thread_reception_string;
import util.FenetreTraitement;
import util.error;
import util.tempo;

public class client {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Charger la bibliothèque OpenCV
    }

    // Variables d'image et d'affichage
    private Mat imageRecu = new Mat();
    private BufferedImage bufferedImage = null;
    private String addressLocalStr;
    private String text = "";

    // Configuration réseau
    private final int[] port = {55000, 55001, 55002};
    private final String address = "172.29.41.9";
    private final String addressBroadcast = "172.29.255.255";
    private byte[] data = new byte[65536];
    private DatagramSocket socketImage;
    private DatagramSocket socketCmd;
    private DatagramPacket packet;
    private InetAddress localAddress;

    // Paramètres image
    private final int[] imgSize = {1280, 720};

    // Variables pour le traitement d'image et l'affichage
    private Mat lastValidImage = null;
    private Mat displayImage = new Mat();
    private Mat resizedValidImage = new Mat();
    private Mat blackImage;

    // Mesure du FPS
    private long previousTime = System.nanoTime();

    // Threads
    private thread_reception_image reception;
    private thread_reception_string cmd;
    private thread.thread_envoie_cmd envoieCmd;

    // Interface graphique
    private FenetreTraitement fenetreClient;

    //--------------------------------------------------------------//
    public client() {
        try {
            initializeNetwork();
            initializeSockets();
            initializeImageProcessing();
            startThreads();
            initializeUI();
            addShutdownHook();
            error.printError();
            mainLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //--------------------------------------------------------------//
    // Initialisation du réseau : récupération de l'adresse IP locale
    private void initializeNetwork() throws Exception {
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
                        this.localAddress = inetAddress;
                        this.addressLocalStr = ip;
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
    }
    //--------------------------------------------------------------//
    // Initialisation des sockets UDP pour l'image et les commandes
    private void initializeSockets() throws SocketException {
        this.socketImage = new DatagramSocket(this.port[1]);
        this.socketCmd = new DatagramSocket(this.port[2]);
        this.packet = new DatagramPacket(this.data, this.data.length);
    }
    //--------------------------------------------------------------//
    // Initialisation des éléments de traitement d'image et création d'une image noire avec "START"
    private void initializeImageProcessing() {
        this.blackImage = new Mat(360, 640, CvType.CV_8UC3, new Scalar(0, 0, 0));
        Size textSize = Imgproc.getTextSize("START", Imgproc.FONT_HERSHEY_SIMPLEX, 2.0, 3, null);
        Point textOrg = new Point(
            (this.blackImage.cols() - textSize.width) / 2,
            (this.blackImage.rows() + textSize.height) / 2
        );
        Imgproc.putText(
            this.blackImage,
            "START",
            textOrg,
            Imgproc.FONT_HERSHEY_SIMPLEX,
            2.0,
            new Scalar(255, 255, 255),
            3
        );
    }
    //--------------------------------------------------------------//
    // Démarrage des threads de réception et d'envoi
    private void startThreads() {
        this.reception = new thread_reception_image("client_UDP_image", this.socketImage, this.imageRecu);
        this.reception.start();

        this.cmd = new thread_reception_string("traitement_UDP_String", this.socketCmd);
        this.cmd.start();

        this.envoieCmd = new thread.thread_envoie_cmd("C", this.addressLocalStr, this.addressBroadcast, this.port[2]);
        this.envoieCmd.start();
    }
    //--------------------------------------------------------------//
    // Initialisation de l'interface graphique
    private void initializeUI() {
        ImageIcon icon = new ImageIcon("lib/logo.png"); // Remplacer par le chemin réel de l'icône
        this.fenetreClient = new FenetreTraitement("client", icon, 1280, 0);
    }
    //--------------------------------------------------------------//
    // Ajout d'un hook pour l'arrêt du programme
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.text = "C#remove?address#" + this.addressLocalStr;
                sendTextUDP(this.text, this.addressBroadcast, this.port[2]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
    //--------------------------------------------------------------//
    // Boucle principale d'affichage
    private void mainLoop() {
        int previousTraitement = 0;
        int currentTraitement = 0;
        while (true) {
            this.imageRecu = this.reception.getImageRecu();

            if (this.imageRecu.empty()) {
                if (this.lastValidImage != null) {
                    this.displayImage = this.resizedValidImage;
                } else {
                    this.displayImage = this.blackImage;
                }
            } else {
                this.lastValidImage = this.imageRecu.clone();
                long currentTime = System.nanoTime();
                double intervalInSeconds = (currentTime - this.previousTime) / 1_000_000_000.0;
                double fps = 1.0 / intervalInSeconds;
                Imgproc.putText(
                    this.lastValidImage,
                    String.format("FPS: %.0f", fps),
                    new Point(10, 60),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    1,
                    new Scalar(255, 0, 0),
                    2
                );
                this.previousTime = currentTime;

                Size displayFrameHalfSize = new Size(this.imageRecu.width() / 2, this.imageRecu.height() / 2);
                Imgproc.resize(this.lastValidImage, this.resizedValidImage, displayFrameHalfSize);
                this.displayImage = this.resizedValidImage;
            }

            try {
                byte[] encodedImage = encodeImageToJPEG(this.displayImage, 100);
                this.bufferedImage = byteArrayToBufferedImage(encodedImage);
                this.fenetreClient.setImage(this.bufferedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            currentTraitement = this.fenetreClient.getTraitement();
            if (currentTraitement != previousTraitement) {
                try {
                    this.text = "C#cmd?address#" + this.addressLocalStr + "?time#" + LocalDateTime.now() + "?traitement#" + currentTraitement;
                    sendTextUDP(this.text, this.addressBroadcast, this.port[2]);
                    previousTraitement = currentTraitement;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            new tempo(10);
        }
    }
    //--------------------------------------------------------------//
    // Méthode pour encoder une image en JPEG avec un taux de compression donné
    private byte[] encodeImageToJPEG(Mat image, int quality) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, matOfByte, new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality));
        return matOfByte.toArray();
    }
    //--------------------------------------------------------------//
    // Méthode pour convertir un tableau d'octets en BufferedImage
    private static BufferedImage byteArrayToBufferedImage(byte[] byteArray) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        return ImageIO.read(bis);
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
            System.out.println("Image envoyée à " + address + ":" + port);
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
    //--------------------------------------------------------------//
    // Méthode pour envoyer un texte via UDP
    private void sendTextUDP(String data, String address, int port) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(); // Crée un socket UDP
            InetAddress ipAddress = InetAddress.getByName(address); // Résolution de l'adresse IP
            byte[] buffer = data.getBytes(); // Conversion du texte en tableau d'octets
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipAddress, port);
            socket.send(packet); // Envoie du paquet UDP
            System.out.println("Données envoyées à " + address + ":" + port);
            System.out.println("Données envoyées : " + data);
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
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
