/**
 * -------------------------------------------------------------------
 * Nom du fichier : drone.java
 * Auteur         : BEAL JULIEN
 * Version        : 1.0
 * Date           : 03/02/2025
 * Description    : code drone pour envoi d'image
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import util.error;

public class drone {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public drone () {
        
        // Définition des ports UDP
        int port[] = {
            55000, // Port de réception traitement
            55001, // Port de réception client
            55002 // Port de commande image
            };
        
        // Définition des adresses IP
        String address = "172.29.41.9";
        String address_broadcast = "172.29.255.255";

        try {
            // Obtenir l'adresse IP locale
            InetAddress address_local = InetAddress.getLocalHost();
            String address_local_str = address_local.getHostAddress();
        } catch (Exception e) {
        
        }
        
        // Définition de la taille de l'image
        int imgsize[] = {1280, 720};
        // Initialisation des sockets
        VideoCapture capture = new VideoCapture(0);
        // Vérification si la caméra est ouverte
        if (!capture.isOpened()) {
            System.out.println("Erreur : Impossible d'ouvrir la caméra.");
            return;
        }
        // Initialisation des matrices
        Mat frame = new Mat();
        byte[] data = new byte[65536];
        // Taille maximale autorisée pour un paquet UDP (en bytes)
        int maxPacketSize = 65528; // 65536 - 8 (overhead UDP)

        long currentTime , previousTime =System.nanoTime() ;
        double intervalInSeconds , fps;

        int quality = 70; // Qualité initiale
        byte[] encodedData;

        //--------------------------------------------------------------//
        //--------------------------------------------------------------//
        //--------------------------- boucle ---------------------------//
        //--------------------------------------------------------------//
        //--------------------------------------------------------------//
        error.printError();
        // Boucle principale
        while (true) {
            // Capture d'une image
            if (!capture.read(frame)) {
                System.out.println("Erreur de capture d'image.");
                break;
            }
            // Redimensionner l'image
            Imgproc.resize(frame, frame, new Size(imgsize[0], imgsize[1]));
            
            // Ajuster dynamiquement le taux de compression
            quality = 70; // Qualité initiale


            // Encoder l'image en JPEG et ajuster la qualité si nécessaire
            do {
                encodedData = encodeImageToJPEG(frame, quality);
                quality -= 5; // Réduire la qualité de compression
            } while (encodedData.length > maxPacketSize && quality > 10); // Réduire jusqu'à ce que l'image tienne dans un paquet UDP
            // Envoi de l'image
            try {
                sendImageUDP(encodedData, address, port[0]);
                currentTime = System.nanoTime();
                intervalInSeconds = (currentTime - previousTime) / 1_000_000_000.0; // Intervalle en secondes
                fps = 1.0 / intervalInSeconds; // Calcul des FPS
                System.out.printf(" FPS: %.0f\n", fps);

                // Mettre à jour le temps précédent
                previousTime = currentTime;

            } catch (IOException e) {
                System.out.println("Erreur lors de l'envoi de l'image : " + e.getMessage());
            }
            // Tempo
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    //--------------------------------------------------------------//
    // Méthode pour envoyer une image via UDP
    private static void sendImageUDP(byte[] imageData, String address, int port) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress ipAddress = InetAddress.getByName(address);
            DatagramPacket packet = new DatagramPacket(imageData, imageData.length, ipAddress, port);
            socket.send(packet);
            System.out.printf("Image envoyée à " + address + ":" + port );
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
    //--------------------------------------------------------------//
    // Méthode pour encoder une image en JPEG avec un taux de compression donné
    private static byte[] encodeImageToJPEG(Mat image, int quality) {
        MatOfByte matOfByte = new MatOfByte();
        // Encoder l'image en JPEG avec un taux de compression spécifique
        Imgcodecs.imencode(".jpg", image, matOfByte, new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality));
        return matOfByte.toArray();
    }
}
