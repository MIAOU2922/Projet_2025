/**
 * -------------------------------------------------------------------
 * Nom du fichier : thread_reception_image.java
 * Auteur         : BEAL JULIEN
 * Version        : 2.0
 * Date           : 11/02/2025
 * Description    : Thread de réception d'image via UDP
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package thread;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import util.tempo;


// Réception de l'image via UDP
public class thread_reception_image extends Thread {
    private DatagramSocket socket;
    private Mat imageRecu;
    private Mat blackImage;
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

        // --------------------------------------------------------------//
        // Initialisation de l'image noire
        try {
            // Création d'une image noire avec le texte "START"
            this.blackImage = new Mat(720, 1080, CvType.CV_8UC3, new Scalar(0, 0, 0));
            Size textSize = Imgproc.getTextSize("No Image", Imgproc.FONT_HERSHEY_SIMPLEX, 2.0, 3, null);
            Point textOrg = new Point((this.blackImage.cols() - textSize.width) / 2,
                    (this.blackImage.rows() + textSize.height) / 2);
            Imgproc.putText(this.blackImage, "No Image", textOrg, Imgproc.FONT_HERSHEY_SIMPLEX, 2.0,
                    new Scalar(255, 255, 255), 3);
        } catch (Exception e) {
            System.out.print("\nErreur lors de l'initialisation de l'image noire");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        while (true) {
            try {
                // Réception de l'image via UDP
                socket.receive(packet);
                this.imageRecu = Imgcodecs.imdecode(new MatOfByte(packet.getData()), Imgcodecs.IMREAD_COLOR);

            } catch (Exception e) {
                System.out.println("Erreur de reception d'image : " + e.getMessage());
            }
        }
    }
    public Mat getImageRecu() {
        if (this.imageRecu == null || this.imageRecu.empty()) {
            return this.blackImage;
        }
        return this.imageRecu;
    }
}
