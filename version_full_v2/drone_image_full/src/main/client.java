/**
 * -------------------------------------------------------------------
 * Nom du fichier : client.java
 * Auteur         : BEAL JULIEN
 * Version        : 1.0
 * Date           : 03/02/2025
 * Description    : code affichage client
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package main;

import java.awt.image.*;
import java.io.*;
import java.net.*;
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

    Mat imageRecu = new Mat();
    BufferedImage bufferedImage = null;
    String address_local_str;

    public client () {
        // Définition des ports UDP
        int port[] = {
            55000, // Port de réception traitement
            55001, // Port de réception client
            55002 // Port de commande image
        };

        // Définition des adresses IP
        String address = "172.29.41.9";
        String address_broadcast = "172.29.255.255";

        // Définition du texte à envoyer
        String text = "";

        byte[] data = new byte[65536];
        DatagramSocket socket_image = null;
        DatagramSocket socket_cmd = null;
        DatagramPacket packet = null;

        int previousTraitement = 0;
        int currentTraitement = 0;

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
            socket_image = new DatagramSocket(port[1]);
            socket_cmd = new DatagramSocket(port[2]);
            packet = new DatagramPacket(data, data.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Thread pour envoyer l'adresse IP locale toutes les 3 minutes
        new Thread(() -> {
            while (true) {
                try {
                    sendTextUDP("address#" + address_local_str, address_broadcast, port[2]);
                    Thread.sleep(500); // attendre 30 secondes
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Définition de la taille de l'image
        int imgsize[] = {1280, 720};
        Mat dermiereImageValide = null;
        Mat Image_a_afficher = new Mat(), dermiereImageValide_resizedImage = new Mat();

        long currentTime, previousTime = System.nanoTime();
        double intervalInSeconds, fps;

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

        thread_reception_image reception = new thread_reception_image("client_UDP_image",socket_image, imageRecu);
        reception.start();

        thread_reception_string cmd = new thread_reception_string("traitement_UDP_String", socket_cmd);
        cmd.start();

        //--------------------------------------------------------------//
        // Charger l'icône depuis les ressources
        ImageIcon icon = new ImageIcon("lib/logo.png"); // Remplace par le chemin réel
        FenetreTraitement client = new FenetreTraitement("client", icon, 1280, 0);

        //--------------------------------------------------------------//
        error.printError();

        // Boucle principale pour afficher l'image reçue
        while (true) {
            this.imageRecu = reception.getImageRecu();

            if (this.imageRecu.empty()) {
                if (dermiereImageValide != null) {
                    Image_a_afficher = dermiereImageValide_resizedImage;
                } else {
                    Image_a_afficher = blackImage;
                }
            } else {
                dermiereImageValide = this.imageRecu.clone();
                currentTime = System.nanoTime();
                intervalInSeconds = (currentTime - previousTime) / 1_000_000_000.0;
                fps = 1.0 / intervalInSeconds;
                Imgproc.putText(dermiereImageValide, String.format("FPS: %.0f", fps), new org.opencv.core.Point(10, 60), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
                previousTime = currentTime;

                Size displayFrameHalfSize = new Size(this.imageRecu.width() / 2, this.imageRecu.height() / 2);
                Imgproc.resize(dermiereImageValide, dermiereImageValide_resizedImage, displayFrameHalfSize);
                Image_a_afficher = dermiereImageValide_resizedImage;
            }

            // Afficher l'image dans la fenêtre
            try {
                bufferedImage = byteArrayToBufferedImage(encodeImageToJPEG(Image_a_afficher, 100));
                client.setImage(bufferedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Envoi du traitement à effectuer uniquement s'il y a eu une modification d'état
            currentTraitement = client.getTraitement();
            if (currentTraitement != previousTraitement) {
                try {
                    text = "address#" + address_local_str + ":" + port[1] + "?traitement#" + currentTraitement + "?time#" + client.getLastModifiedTime();
                    sendTextUDP(text, address_broadcast, port[2]);
                    previousTraitement = currentTraitement; // Mettre à jour l'état précédent
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
    // Méthode pour envoyer un String via UDP
    private void sendTextUDP(String data, String address, int port) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(); // Crée un socket UDP
            InetAddress ipAddress = InetAddress.getByName(address); // Résolution de l'adresse IP
            
            byte[] buffer = data.getBytes(); // Convertir le texte en tableau d'octets
            
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipAddress, port);
            socket.send(packet); // Envoie du paquet UDP
            
            System.out.println("Données envoyées à " + address + ":" + port);
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Ferme le socket proprement
            }
        }
    }
}
