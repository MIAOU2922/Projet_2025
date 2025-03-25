/**
 * -------------------------------------------------------------------
 * Nom du fichier : drone.java
 * Auteur         : BEAL JULIEN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Code drone pour envoi d'image
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package main;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import thread.*;
import util.*;

public class drone_L {

    static {

        try {
            // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            try {

                String libPath = System.getProperty("user.dir") + "/lib/libopencv_java4100.so";
                System.load(libPath);

            } catch (Exception e) {
                System.out.println("1");
            }
            try {

                String libPath = System.getProperty("user.dir") + "/lib/libopencv_core.so.4.10.0";
                System.load(libPath);

            } catch (Exception e) {
                System.out.println("2");
            }
            try {

                String libPath = System.getProperty("user.dir") + "/lib/libopencv_videoio.so.4.10.0";
                System.load(libPath);

            } catch (Exception e) {
                System.out.println("3");
            }
            try {

                String libPath = System.getProperty("user.dir") + "/lib/libopencv_video.so.4.10.0";
                System.load(libPath);

            } catch (Exception e) {
                System.out.println("4");
            }
            try {

                String libPath = System.getProperty("user.dir") + "/lib/libopencv_xphoto.so.410";
                System.load(libPath);

            } catch (Exception e) {
                System.out.println("5");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors du chargement des librairies: " + e);
        }
    }

    // Définition des ports UDP
    private int[] port = { 55000, 55001, 55002 };

    // Définition des adresses IP
    private String address = "";

    // Variables réseau
    private InetAddress addressLocal = null;
    private String addressLocalStr = "";

    // Variables UDP
    private byte[] data = new byte[65536];
    private DatagramSocket socketCmd;
    private DatagramPacket packet;

    // Variables de gestion d'image
    private int[] imgSize = { 1280, 720 };
    private int maxPacketSize = 65528; // Taille maximale d'un paquet UDP
    private long last_update_quality;
    private int intervale_update_quality = 2000;
    private int initial_quality = 60;
    private int quality; // Qualité initiale de compression JPEG
    private VideoCapture capture;
    private Mat frame;

    // Variables de gestion du temps
    private long previousTime = System.nanoTime();

    // Threads
    private thread_reception_string commande;
    private thread_list_dynamic_ip listDynamicIp;

    private long t0, t1, t2;

    // --------------------------------------------------------------//
    public drone_L() {

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
                            this.addressLocal = inetAddress;
                            this.addressLocalStr = ip;
                            break;
                        }
                    }
                }
                if (this.addressLocal != null) {
                    break;
                }
            }

            if (this.addressLocal == null) {
                throw new Exception("Aucune adresse IP locale valide trouvée.");
            }

            this.socketCmd = new DatagramSocket(this.port[2]);
            this.packet = new DatagramPacket(this.data, this.data.length);
        } catch (Exception e) {
            System.out.println("Erreur lors de l'initialisation des adresses IP et des sockets UDP");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // Initialisation de la caméra
        try {
            this.capture = new VideoCapture(0);
            if (!this.capture.isOpened()) {
                System.out.println("Erreur : Impossible d'ouvrir la caméra.");
                return;
            }
            this.frame = new Mat();
        } catch (Exception e) {
            System.out.println("Erreur lors de l'initialisation de la caméra");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // Lancement des threads
        try {
            this.commande = new thread_reception_string("traitement_UDP_String", this.socketCmd);
            this.commande.start();

            this.listDynamicIp = new thread_list_dynamic_ip("drone - boucle de vérification de la liste d'adresses");
            this.listDynamicIp.start();
        } catch (Exception e) {
            System.out.println("Erreur lors du lancement des threads");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // Boucle principale du drone
        try {
            error.printError();
            this.mainLoop();
        } catch (Exception e) {
            System.out.println("Erreur lors de l'exécution de la boucle principale");
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------//
    // Boucle principale
    private void mainLoop() {

        long currentTime;
        long previousTime = System.currentTimeMillis();

        while (true) {
            if (!this.capture.read(this.frame)) {
                System.out.println("Erreur de capture d'image.");
                break;
            }

            this.processReceivedMessage();
            this.sendImage();

            currentTime = System.currentTimeMillis();
            System.out.println("fps " + (1000 / (currentTime - previousTime)));
            previousTime = currentTime;

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // --------------------------------------------------------------//
    // Traitement des messages reçus
    private void processReceivedMessage() {
        String messageRecu = this.commande.getMessageRecu();
        if (messageRecu.startsWith("T#")) {
            String[] parts = messageRecu.split("\\?");
            String action = parts[0].split("#")[1];

            if (action.equals("add")) {
                this.listDynamicIp.addClient(parts[1].split("#")[1], parts[2].split("#")[1]);
            } else if (action.equals("remove")) {
                this.listDynamicIp.removeClient(parts[1].split("#")[1]);
            }
        }
    }

    // --------------------------------------------------------------//
    // Envoi de l'image
    private void sendImage() {
        Imgproc.resize(this.frame, this.frame, new Size(this.imgSize[0], this.imgSize[1]));

        /*
        if (System.currentTimeMillis() - this.last_update_quality > this.intervale_update_quality) {
            this.last_update_quality = System.currentTimeMillis();
            this.quality = this.initial_quality;
        }

        */
        byte[] encodedData;
        encodedData = encodeImageToJPEG(this.frame, 50);

        /*
        this.quality += 5;
        do {
            this.quality -= 5;
            encodedData = encodeImageToJPEG(this.frame, this.quality);

        } while (encodedData.length > this.maxPacketSize && this.quality > 10);
        */

        if (!this.listDynamicIp.getClientAddress().isEmpty()) {
            for (String addr : this.listDynamicIp.getClientAddress()) {
                try {
                    this.sendImageUDP(encodedData, addr, this.port[0]);

                } catch (IOException e) {
                    System.out.println("Erreur lors de l'envoi de l'image : " + e.getMessage());
                }
            }
        }
    }

    // --------------------------------------------------------------//
    // Méthode pour envoyer une image via UDP
    private void sendImageUDP(byte[] imageData, String address, int port) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress ipAddress = InetAddress.getByName(address);
            DatagramPacket packet = new DatagramPacket(imageData, imageData.length, ipAddress, port);
            socket.send(packet);
        }
    }

    // --------------------------------------------------------------//
    // Méthode pour encoder une image en JPEG
    private static byte[] encodeImageToJPEG(Mat image, int quality) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, matOfByte, new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality));
        return matOfByte.toArray();
    }
}
