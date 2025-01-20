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

public class drone {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) throws Exception {
        // Définition des ports et adresses IP
        int port[] = {4000, 4001, 4002, 4003, 4004, 4005, 4006, 4007, 4008, 4009};
        String address = "172.29.41.9";
        String address_broadcast = "172.29.255.255";
        // Obtenir l'adresse IP locale
        InetAddress address_local = InetAddress.getLocalHost();
        String address_local_str = address_local.getHostAddress();
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
        //--------------------------------------------------------------//
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
            int quality = 70; // Qualité initiale
            byte[] encodedData;
            // Encoder l'image en JPEG et ajuster la qualité si nécessaire
            do {
                encodedData = encodeImageToJPEG(frame, quality);
                quality -= 5; // Réduire la qualité de compression
            } while (encodedData.length > maxPacketSize && quality > 10); // Réduire jusqu'à ce que l'image tienne dans un paquet UDP
            // Envoi de l'image
            try {
                sendImageUDP(encodedData, address_local_str, port[0]);
            } catch (IOException e) {
                System.out.println("Erreur lors de l'envoi de l'image : " + e.getMessage());
            }
            // Tempo
            try {
                Thread.sleep(10);
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
            System.out.println("Image envoyée à " + address + ":" + port);
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
