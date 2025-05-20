/**
 * -------------------------------------------------------------------
 * Nom du fichier : thread_reception_string.java
 * Auteur         : BEAL JULIEN
 * Version        : 2.0
 * Date           : 11/02/2025
 * Description    : Thread de réception de messages texte via UDP
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package thread;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

// Réception d'un message texte via UDP
public class thread_reception_string extends Thread {
    private DatagramSocket socket;
    private String messageRecu ;
    private String name;

    public thread_reception_string(String _name , DatagramSocket _socket) {
        this.socket = _socket;
        this.messageRecu = "" ;
        this.name = _name;
    }

    @Override
    public void run() {
        byte[] data = new byte[1024]; // Taille ajustée pour un message texte
        DatagramPacket packet = new DatagramPacket(data, data.length);
        Thread.currentThread().setName(name);

        while (true) {
            try {
                // Réception du message texte via UDP
                socket.receive(packet);

                // Convertir les données en String
                messageRecu = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                // Affichage du message reçu
                //System.out.println("\nMessage reçu : " + messageRecu);

            } catch (Exception e) {
                System.out.println("\nErreur de réception du message : " + e.getMessage());
            }
        }
    }

    public String getMessageRecu() {
        return this.messageRecu;
    }
}
