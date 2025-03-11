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

import thread.thread_reception_string;
import util.error;

public class drone {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private ArrayList <String> client_address = new ArrayList<>();
    private ArrayList <String> client_time = new ArrayList<>();


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

        String messageRecu ;
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

            if (messageRecu.startsWith("client#")) {
                parts = messageRecu.split("\\?");
                boolean isTraitement = false;
                
                // Parcourir chaque partie de la trame
                for (String part : parts) {
                    if (part.startsWith("traitement#")) {
                        // Pour la partie "client", on peut éventuellement extraire des infos spécifiques
                        // ou simplement l'ignorer, car elle sert de signal pour lancer le traitement.
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
                // Le message ne commence pas par "traitement#" : aucun traitement n'est effectué.
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

            // Envoi de l'image à chaque adresse dans la liste
            if (!client_address.isEmpty()) {
                for (String addr : client_address) {
                    try {
                        sendImageUDP(encodedData, addr, port[0]);
                        currentTime = System.nanoTime();
                        intervalInSeconds = (currentTime - previousTime) / 1_000_000_000.0; // Intervalle en secondes
                        fps = 1.0 / intervalInSeconds; // Calcul des FPS
                        System.out.printf(" FPS: %.0f\n", fps);
        
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
