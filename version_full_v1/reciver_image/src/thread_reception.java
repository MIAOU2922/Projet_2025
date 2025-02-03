import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;


// Réception de l'image via UDP
public class thread_reception extends Thread {
    private DatagramSocket socket;
    private Mat imageRecu;

    public thread_reception(DatagramSocket _socket , Mat _imageRecu) {
        this.socket = _socket;
        this.imageRecu = _imageRecu;
    }

    @Override
    public void run() {
        byte[] data = new byte[65536];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        while (true) {
            try {
                // Réception de l'image via UDP
                socket.receive(packet);
                this.imageRecu = Imgcodecs.imdecode(new MatOfByte(packet.getData()), Imgcodecs.IMREAD_COLOR);

            } catch (Exception e) {
                System.out.println("Erreur de réception d'image : " + e.getMessage());
            }
        }
    }
    public Mat getImageRecu() {
        return this.imageRecu;
    }
}



