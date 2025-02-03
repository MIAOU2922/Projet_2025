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

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class client {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Charger la bibliothèque OpenCV
    }

    Mat imageRecu = new Mat();
    BufferedImage bufferedImage = null;

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

        byte[] data = new byte[65536];
        DatagramSocket socket = null;
        DatagramPacket packet = null;

        try {
            // Initialisation du socket UDP
            socket = new DatagramSocket(port[1]);
            packet = new DatagramPacket(data, data.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Définition de la taille de l'image
        int imgsize[] = {1280, 720};
        Mat processedImage, processedImage2, dermiereImageValide = null;
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

        thread_reception reception = new thread_reception(socket, imageRecu);
        reception.start();

        //--------------------------------------------------------------//
        // Création de la fenêtre client pour afficher l'image reçue
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 500);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        frame.setLocation(1280,0);

        // Charger l'icône depuis les ressources
        ImageIcon icon = new ImageIcon("lib/logo.png"); // Remplace par le chemin réel
        frame.setIconImage(icon.getImage());

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bufferedImage != null) {
                    g.drawImage(bufferedImage, 0, 0, null);
                }
            }
        };
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);

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

            try {
                bufferedImage = byteArrayToBufferedImage(encodeImageToJPEG(Image_a_afficher, 100));
                SwingUtilities.invokeLater(() -> panel.repaint());
            } catch (IOException e) {
                e.printStackTrace();
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
}
