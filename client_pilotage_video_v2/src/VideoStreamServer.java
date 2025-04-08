import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;

public class VideoStreamServer extends Thread {
	private VideoPanel panel;
    private static int port;
    private static boolean running = true;

    public VideoStreamServer(VideoPanel panel, int port) {
        this.panel = panel;
        this.port = port;
    }
    
    public static void stopServer() {
        running = false;
        try {
            new Socket("localhost", port).close(); // Force l'arrêt en ouvrant une connexion
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (running) {
                try (Socket clientSocket = serverSocket.accept();
                     InputStream inputStream = clientSocket.getInputStream()) {
                    BufferedImage image = ImageIO.read(inputStream);
                    if (image != null) {
                        panel.updateImage(image);
                    }
                } catch (IOException e) {
                	if (running) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
