import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class CameraUDPStreamer {
    static {
        // Chargez la bibliothèque native OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Paramètres UDP
        String udpIp = "127.0.0.1"; // Adresse IP du destinataire
        int udpPort = 5005;         // Port du destinataire

        // Initialisation de la caméra
        VideoCapture camera = new VideoCapture(0); // 0 pour la caméra par défaut
        if (!camera.isOpened()) {
            System.out.println("Impossible d'ouvrir la caméra !");
            return;
        }

        // Initialisation des objets OpenCV
        Mat frame = new Mat();

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(udpIp);

            while (true) {
                // Capture d'une image
                camera.read(frame);
                if (frame.empty()) {
                    System.out.println("Image non capturée.");
                    continue;
                }

                // Compression en JPEG
                MatOfByte buffer = new MatOfByte();
                Imgcodecs.imencode(".jpg", frame, buffer);

                byte[] imageData = buffer.toArray();

                // Segmentation des données pour l'envoi UDP
                int chunkSize = 1400; // Taille maximale d'un paquet UDP
                for (int i = 0; i < imageData.length; i += chunkSize) {
                    int length = Math.min(chunkSize, imageData.length - i);
                    byte[] chunk = new byte[length];
                    System.arraycopy(imageData, i, chunk, 0, length);

                    // Création et envoi du paquet UDP
                    DatagramPacket packet = new DatagramPacket(chunk, chunk.length, address, udpPort);
                    socket.send(packet);
                }

                System.out.println("Image transmise (" + imageData.length + " octets)");

                // Pause pour simuler une fréquence d'images (~30 FPS)
                Thread.sleep(33); // ~33 ms pour 30 FPS
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Libérer les ressources
            camera.release();
        }
    }
}
