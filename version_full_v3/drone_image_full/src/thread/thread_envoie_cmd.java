/**
 * -------------------------------------------------------------------
 * Nom du fichier : thread_envoie_cmd.java
 * Auteur         : BEAL JULIEN
 * Version        : 1.0
 * Date           : 14/03/2025
 * Description    : Thread d'envoie de commandes UDP
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package thread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;

public class thread_envoie_cmd extends Thread {

    private String type; // permet d'identifier de quel type est le device (T = traitement , C = client )
    private String address_local_str; // adresse IP locale
    private String address_broadcast; // adresse IP de broadcast
    private int port; // port de communication
    private String text;

    public thread_envoie_cmd(String type, String address_local_str, String address_broadcast, int port) {
        this.type = type;
        this.address_local_str = address_local_str;
        this.address_broadcast = address_broadcast;
        this.port = port;
        this.text = "";
    }

    @Override
    public void run() {
        Thread.currentThread().setName("boucle d'ajout a liste");
        while (true) {
            try {
                this.text = type +"#add?address#" + this.address_local_str + "?time#" + LocalDateTime.now();
                this.sendTextUDP(this.text, this.address_broadcast, this.port);
                Thread.sleep(30000); // attendre 30 secondes
            } catch (Exception e) {
                e.printStackTrace();
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
            
            // System.out.println("\nDonnées envoyées à " + address + ":" + port );
            // System.out.println("\nDonnées envoyées : " + data);

        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Ferme le socket proprement
            }
        }
    }
}