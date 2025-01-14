import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class cmd {
    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    String message = "command";
                    byte[] buffer = message.getBytes();
                    InetAddress address = InetAddress.getByName("255.255.255.255");
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 8889);
                    socket.send(packet);
                    socket.close();
                    System.out.println("Command sent: " + message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000); // 0ms delay, 1000ms period (1 second)
    }
}
