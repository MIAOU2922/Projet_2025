/**
 * -------------------------------------------------------------------
 * Nom du fichier : thread_reception.java
 * Auteur         : BEAL JULIEN
 * Version        : 1.0
 * Date           : 03/02/2025
 * Description    : thread de reception d'image via UDP
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package thread;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;


// Réception de l'image via UDP
public class thread_reception_image extends Thread {
    private DatagramSocket socket;
    private Mat imageRecu;
    private String name;

    public thread_reception_image(String _name , DatagramSocket _socket , Mat _imageRecu) {
        this.socket = _socket;
        this.imageRecu = _imageRecu;
        this.name = _name ;
    }

    @Override
    public void run() {
        byte[] data = new byte[65536];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        Thread.currentThread().setName(name);
        while (true) {
            try {
                // Réception de l'image via UDP
                socket.receive(packet);
                this.imageRecu = Imgcodecs.imdecode(new MatOfByte(packet.getData()), Imgcodecs.IMREAD_COLOR);

            } catch (Exception e) {
                //System.out.println("Erreur de réception d'image : " + e.getMessage());
            }
        }
    }
    public Mat getImageRecu() {
        return this.imageRecu;
    }
}



