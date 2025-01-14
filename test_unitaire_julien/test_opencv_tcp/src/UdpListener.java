import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpListener implements Runnable {
    private int port;
    private byte[] buffer;
    private ImageReceiver receiver;

    public UdpListener(int port, byte[] buffer, ImageReceiver receiver) {
        this.port = port;
        this.buffer = buffer;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                receiver.processPacket(packet, port);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}