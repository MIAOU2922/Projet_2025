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

import java.io.*;
import java.net.*;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import thread.*;
import util.*;
import gpio.*;

public class drone_video {

    static {

        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (Exception e) {
            System.out.println("\nErreur lors du chargement des librairies: " + e);
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
    private byte[] encodedData;
    // time
    private long currentTime = System.currentTimeMillis();
    private long previousTime = System.currentTimeMillis();
    // Threads
    private thread_reception_string commande;
    private thread_list_dynamic_ip listDynamicIp;
    private drone_telemetrie telemetrie;
    private Serveur_Char_GPIO gpio;

    // --------------------------------------------------------------//
    public drone_video() {
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
            System.out.println("\nErreur lors de l'initialisation des adresses IP et des sockets UDP");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // Initialisation de la caméra
        try {
            this.capture = new VideoCapture(0, Videoio.CAP_V4L2);
            if (!this.capture.isOpened()) {
                System.out.println("\nErreur : Impossible d'ouvrir la caméra.");
                return;
            }
            this.frame = new Mat();
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'initialisation de la caméra");
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
            System.out.println("\nErreur lors du lancement des threads");
            e.printStackTrace();
        }

        Thread telemetrieThread = new Thread(() -> {
            try {
                // On instancie et démarre la télémétrie
                drone_telemetrie telemetrie = new drone_telemetrie(this.commande, this.listDynamicIp);
                telemetrie.start();
            } catch (Exception e) {
                System.err.println("Erreur lors du démarrage de la télémétrie");
                e.printStackTrace();
            }
        }, "Thread-Drone-Telemetrie");
        telemetrieThread.start();

        new tempo(2);
        try {
            Serveur_Char_GPIO gpio = new Serveur_Char_GPIO(telemetrie);
            gpio.start();
        }catch (Exception e) {
            System.out.println("\nErreur");
            e.printStackTrace();
        }


        // --------------------------------------------------------------//
        // Boucle principale du drone
        try {
            error.printError();
            this.mainLoop();
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'exécution de la boucle principale");
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------//
    // Boucle principale
    private void mainLoop() {
        while (true) {
            if (!this.capture.read(this.frame)) {
                System.out.println("\nErreur de capture d'image.");
                break;
            }
            this.processReceivedMessage();
            this.sendImage();
            currentTime = System.currentTimeMillis();
            System.out.print("\033[K"); // Efface la ligne
            System.out.print(String.format("\rfps: %d   ", (1000 / (currentTime - previousTime))));

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

        // Reset la qualité à la valeur initiale au début de chaque boucle
        int currentQuality = this.initial_quality;

        do {
            encodedData = encodeImageToJPEG(this.frame, currentQuality);
            if (encodedData.length > maxPacketSize) {
                currentQuality -= 5;  // Réduire la qualité si trop grande
                if (currentQuality < 5) {
                    System.out.println("\rQualité trop basse, image ignorée");
                    return;
                }
            }
        } while (encodedData.length > maxPacketSize);

        // Afficher si la qualité a dû être ajustée
        if (currentQuality != this.initial_quality) {
            //System.out.println("\rQualité ajustée à: " + currentQuality);
        }

        if (!this.listDynamicIp.getClientAddress().isEmpty()) {
            for (String addr : this.listDynamicIp.getClientAddress()) {
                try {
                    this.sendImageUDP(encodedData, addr, this.port[0]);
                } catch (IOException e) {
                    System.out.println("\nErreur lors de l'envoi de l'image : " + e.getMessage());
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
