/**
 * -------------------------------------------------------------------
 * Nom du fichier : drone.java
 * Auteur         : BEAL JULIEN
 * Version        : 2.0
 * Date           : 11/02/2025
 * Description    : Code drone pour envoi d'image
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Enumeration;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import thread.*;
import util.*;

public class drone_W {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public drone_W () {
        
        // Définition des ports UDP
        int port[] = {
            55000, // Port de réception traitement
            55001, // Port de réception client
            55002 // Port de commande image
            };
        
        // Définition des adresses IP
        String address = "172.29.41.9";
        String address_broadcast = "172.29.255.255";

        String messageRecu , action;
        String[] parts;
        LocalDateTime Client_Time = LocalDateTime.now() , update_afk = LocalDateTime.now();
        
        InetAddress address_local = null;
        String address_local_str = "";
        byte[] data = new byte[65536];
        DatagramSocket socket_cmd = null;
        DatagramPacket packet = null;

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

            socket_cmd = new DatagramSocket(port[2]);
            packet = new DatagramPacket(data, data.length);
        } catch (Exception e) {
            e.printStackTrace();
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
        // Taille maximale autorisée pour un paquet UDP (en bytes)
        int maxPacketSize = 65528; // 65536 - 8 (overhead UDP)

        long currentTime , previousTime =System.nanoTime() ;
        double intervalInSeconds , fps;

        int quality = 70; // Qualité initiale
        byte[] encodedData;

        thread_reception_string commande = new thread_reception_string("traitement_UDP_String", socket_cmd);
        commande.start();
        
        thread_list_dynamic_ip list_dynamic_ip = new thread_list_dynamic_ip("drone - boucle de vérification de la liste d'addresses");
        list_dynamic_ip.start();


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

            // Traitement du message reçu
            messageRecu = commande.getMessageRecu();

            if (messageRecu.startsWith("T#")){
                parts = messageRecu.split("\\?");

                action = parts[0].split("#")[1];

                if (action.equals("add")){
                
                list_dynamic_ip.addClient(parts[1].split("#")[1], parts[2].split("#")[1]);
                
                } else if (action.equals("remove")){
                
                list_dynamic_ip.removeClient(parts[1].split("#")[1]);
                

                }
            }
            // Réinitialisation des variables
            messageRecu = "";
            parts = null;

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

            // Envoi de l'image à chaque adresse dans la liste
            if (!list_dynamic_ip.getClientAddress().isEmpty()) {
                for (String addr : list_dynamic_ip.getClientAddress()) {
                    try {
                        sendImageUDP(encodedData, addr, port[0]);
                        currentTime = System.nanoTime();
                        intervalInSeconds = (currentTime - previousTime) / 1_000_000_000.0; // Intervalle en secondes
                        fps = 1.0 / intervalInSeconds; // Calcul des FPS
                        //System.out.printf(" FPS: %.0f\n", fps);
        
                        // Mettre à jour le temps précédent
                        previousTime = currentTime;
        
                    } catch (IOException e) {
                        System.out.println("Erreur lors de l'envoi de l'image : " + e.getMessage());
                    }
                }
            } else {
                //System.out.println("La liste des adresses est vide, aucune image n'a été envoyée.");
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
    private void sendImageUDP(byte[] imageData, String address, int port) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress ipAddress = InetAddress.getByName(address);
            DatagramPacket packet = new DatagramPacket(imageData, imageData.length, ipAddress, port);
            socket.send(packet);
            //System.out.println("Image envoyée à " + address + ":" + port );
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
    private static byte[] encodeImageToJPEG(Mat image, int quality) {
        MatOfByte matOfByte = new MatOfByte();
        // Encoder l'image en JPEG avec un taux de compression spécifique
        Imgcodecs.imencode(".jpg", image, matOfByte, new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality));
        return matOfByte.toArray();
    }
}
