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

package thread;

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

import util.error;
import util.tempo;

public class client extends Thread {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Charger la bibliothèque OpenCV
    }
    // Variables d'image et d'affichage
    private Mat imageRecu = new Mat();
    private BufferedImage bufferedImage = null;
    private String addressLocalStr;
    private String text = "";
    // Configuration réseau
    private int[] port = {55000, 55001, 55002};
    private String address = "172.29.41.9";
    private String addressBroadcast = "172.29.255.255";
    private byte[] data = new byte[65536];
    private DatagramSocket socketImage;
    private DatagramSocket socketCmd;
    private DatagramPacket packet;
    private InetAddress localAddress;
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
    
    String prefix = "C";

    //--------------------------------------------------------------//
    public client() {
        super("Client_Thread"); // Ajout du nom du thread
        initialize();
    }

    private void initialize() {
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
            this.socketImage = new DatagramSocket(this.port[1]);
            this.socketCmd = new DatagramSocket(this.port[2]);
            this.packet = new DatagramPacket(this.data, this.data.length);
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'initialisation des adresses IP et des sockets UDP");
            e.printStackTrace();
        }
        //--------------------------------------------------------------//
        // Initialisation de l'image noire
        try {
            // Création d'une image noire avec le texte "START"
            this.blackImage = new Mat(360, 640, CvType.CV_8UC3, new Scalar(0, 0, 0));
            Size textSize = Imgproc.getTextSize("No Image", Imgproc.FONT_HERSHEY_SIMPLEX, 2.0, 3, null);
            Point textOrg = new Point((this.blackImage.cols() - textSize.width) / 2,(this.blackImage.rows() + textSize.height) / 2);
            Imgproc.putText(this.blackImage,"No Image",textOrg,Imgproc.FONT_HERSHEY_SIMPLEX,2.0,new Scalar(255, 255, 255),3);
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'initialisation de l'image noire");
            e.printStackTrace();
        }
        //--------------------------------------------------------------//
        // lancement des threads
        try {
            //image
            this.reception = new thread_reception_image("client_UDP_image", this.socketImage, this.imageRecu);
            this.reception.start();
            //reception commande
            this.cmd = new thread_reception_string("traitement_UDP_String", this.socketCmd);
            this.cmd.start();
            //envoie commande
            this.envoieCmd = new thread.thread_envoie_cmd(prefix, this.addressLocalStr, this.addressBroadcast, this.port[2]);
            this.envoieCmd.start();
        } catch (Exception e) {
            System.out.println("\nErreur lors du lancement des threads");
            e.printStackTrace();
        }
        //--------------------------------------------------------------//
        // Ajout du shutdown hook pour supprimer l'adresse de la liste
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    this.text = prefix +"#remove?address#" + this.addressLocalStr;
                    sendTextUDP(this.text, this.addressBroadcast, this.port[2]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'ajout du shutdown hook");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            error.printError();
            mainLoop();
        } catch (Exception e) {
            System.out.println("\nErreur lors de la boucle principale du client");
            e.printStackTrace();
        }
    }

    // Méthode pour arrêter proprement le thread
    public void stopClient() {
        // Envoi du message de déconnexion
        try {
            this.text = prefix+"#remove?address#" + this.addressLocalStr;
            sendTextUDP(this.text, this.addressBroadcast, this.port[2]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Arrêt des threads
        if (this.reception != null) this.reception.interrupt();
        if (this.cmd != null) this.cmd.interrupt();
        if (this.envoieCmd != null) this.envoieCmd.interrupt();
        
        // Fermeture des sockets
        if (this.socketImage != null) this.socketImage.close();
        if (this.socketCmd != null) this.socketCmd.close();
        
        // Interruption du thread principal
        this.interrupt();
    }

    //--------------------------------------------------------------//
    // Boucle principale d'affichage
    private void mainLoop() {
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
                System.out.print("\033[K");  // Efface la ligne
                System.out.print(String.format("\rfps: %.0f   ", fps));
                this.previousTime = currentTime;
                Size displayFrameHalfSize = new Size(this.imageRecu.width() / 2, this.imageRecu.height() / 2);
                Imgproc.resize(this.lastValidImage, this.resizedValidImage, displayFrameHalfSize);
                this.displayImage = this.resizedValidImage;
            }
            try {
                byte[] encodedImage = encodeImageToJPEG(this.displayImage, 100);
                this.bufferedImage = byteArrayToBufferedImage(encodedImage);
                //ajouté une methode pour mettre à jour l'image dans la fenêtre
                //this.fenetreClient.setImage(this.bufferedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            new tempo(1);
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
        }finally {
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
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        }
        return ip;
    }
    //--------------------------------------------------------------//
    // Méthode pour obtenir l'image courante
    public BufferedImage getCurrentImage() {
        return this.bufferedImage;
    }
}
