import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ImageReceiver {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("OpenCV library loaded successfully.");
    }

    public static void main(String[] args) {
        int port = 4903;
        byte[] buffer = new byte[65536]; // Buffer to hold incoming data

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Listening on port " + port + " for incoming images...");

            // Créer une fenêtre non redimensionnable
            HighGui.namedWindow("Receiver", HighGui.WINDOW_NORMAL);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Save the received data to a file
                String filePath = "received_image.jpg";
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(packet.getData(), 0, packet.getLength());
                }

                // Load the image using OpenCV
                Mat receivedImage = Imgcodecs.imread(filePath);

                if (!receivedImage.empty()) {
                    // Redimensionner l'image pour l'affichage à la moitié de la taille
                    Mat displayFrameHalfSize = new Mat();
                    Imgproc.resize(receivedImage, displayFrameHalfSize, new Size(receivedImage.width() / 2, receivedImage.height() / 2));

                    // Afficher l'image redimensionnée
                    HighGui.imshow("Receiver", displayFrameHalfSize);
                    HighGui.waitKey(1); // Refresh the window
                } else {
                    System.out.println("Failed to load the received image.");
                }
            }
        } catch (SocketException e) {
            System.err.println("Socket error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
        }
    }
}