import javax.swing.*;
import java.util.Random;
import java.awt.event.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class telemetrieSender {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static String address;
    private static int port_telemetry;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Ask if broadcast mode is to be used
        System.out.print("Use broadcast mode? (yes/no): ");
        String broadcastMode = scanner.nextLine().trim().toLowerCase();
        boolean isBroadcast = broadcastMode.equals("yes");

        // Ask for the reference port
        System.out.print("Enter the reference port: ");
        int referencePort = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        port_telemetry = referencePort + 2;

        if (isBroadcast) {
            address = "172.29.255.255"; // Replace with your network's broadcast address
        } else {
            System.out.print("Enter the receiver's IP address: ");
            address = scanner.nextLine().trim();
        }

        // Créer le frame
        JFrame frame = new JFrame("IHM Capteur Virtuel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 1940);
        
        // Créer le panel
        JPanel panel = new JPanel();
        panel.setLayout(null);

        // Ajouter le label "capteur virtuel"
        JLabel capteurVirtuelLabel = new JLabel("Capteur virtuel");
        capteurVirtuelLabel.setBounds(10, 10, 100, 20);
        panel.add(capteurVirtuelLabel);

        // Ajouter le label et la case "random global"
        JLabel randomGlobalLabel = new JLabel("Random global:");
        randomGlobalLabel.setBounds(285, 10, 100, 20);
        panel.add(randomGlobalLabel);

        JCheckBox randomGlobalCheckBox = new JCheckBox();
        randomGlobalCheckBox.setBounds(370, 10, 20, 20);
        panel.add(randomGlobalCheckBox);

        // Ajouter sliders et contrôles associés
        JSlider midSlider = createSlider(panel, "mid:", 40);
        JCheckBox midCheckBox = createCheckBox(panel, "random:", 40);
        JSlider xSlider = createSlider(panel, "x:", 90);
        JCheckBox xCheckBox = createCheckBox(panel, "random:", 90);
        JSlider ySlider = createSlider(panel, "y:", 140);
        JCheckBox yCheckBox = createCheckBox(panel, "random:", 140);
        JSlider zSlider = createSlider(panel, "z:", 190);
        JCheckBox zCheckBox = createCheckBox(panel, "random:", 190);
        JSlider mprySlider = createSlider(panel, "mpry:", 240);
        JCheckBox mpryCheckBox = createCheckBox(panel, "random:", 240);
        JSlider pitchSlider = createSlider(panel, "pitch:", 290);
        JCheckBox pitchCheckBox = createCheckBox(panel, "random:", 290);
        JSlider rollSlider = createSlider(panel, "roll:", 340);
        JCheckBox rollCheckBox = createCheckBox(panel, "random:", 340);
        JSlider yawSlider = createSlider(panel, "yaw:", 390);
        JCheckBox yawCheckBox = createCheckBox(panel, "random:", 390);
        JSlider vgxSlider = createSlider(panel, "vgx:", 440);
        JCheckBox vgxCheckBox = createCheckBox(panel, "random:", 440);
        JSlider vgySlider = createSlider(panel, "vgy:", 490);
        JCheckBox vgyCheckBox = createCheckBox(panel, "random:", 490);
        JSlider vgzSlider = createSlider(panel, "vgz:", 540);
        JCheckBox vgzCheckBox = createCheckBox(panel, "random:", 540);
        JSlider templSlider = createSlider(panel, "templ:", 590);
        JCheckBox templCheckBox = createCheckBox(panel, "random:", 590);
        JSlider temphSlider = createSlider(panel, "temph:", 640);
        JCheckBox temphCheckBox = createCheckBox(panel, "random:", 640);
        JSlider tofSlider = createSlider(panel, "tof:", 690);
        JCheckBox tofCheckBox = createCheckBox(panel, "random:", 690);
        JSlider hSlider = createSlider(panel, "h:", 740);
        JCheckBox hCheckBox = createCheckBox(panel, "random:", 740);
        JSlider batSlider = createSlider(panel, "bat:", 790);
        JCheckBox batCheckBox = createCheckBox(panel, "random:", 790);
        JSlider baroSlider = createSlider(panel, "baro:", 840);
        JCheckBox baroCheckBox = createCheckBox(panel, "random:", 840);
        JSlider agxSlider = createSlider(panel, "agx:", 890);
        JCheckBox agxCheckBox = createCheckBox(panel, "random:", 890);
        JSlider agySlider = createSlider(panel, "agy:", 940);
        JCheckBox agyCheckBox = createCheckBox(panel, "random:", 940);
        JSlider agzSlider = createSlider(panel, "agz:", 990);
        JCheckBox agzCheckBox = createCheckBox(panel, "random:", 990);

        // Ajouter des timers
        initializeTimers(midSlider, midCheckBox, randomGlobalCheckBox);
        initializeTimers(xSlider, xCheckBox, randomGlobalCheckBox);
        initializeTimers(ySlider, yCheckBox, randomGlobalCheckBox);
        initializeTimers(zSlider, zCheckBox, randomGlobalCheckBox);
        initializeTimers(mprySlider, mpryCheckBox, randomGlobalCheckBox);
        initializeTimers(pitchSlider, pitchCheckBox, randomGlobalCheckBox);
        initializeTimers(rollSlider, rollCheckBox, randomGlobalCheckBox);
        initializeTimers(yawSlider, yawCheckBox, randomGlobalCheckBox);
        initializeTimers(vgxSlider, vgxCheckBox, randomGlobalCheckBox);
        initializeTimers(vgySlider, vgyCheckBox, randomGlobalCheckBox);
        initializeTimers(vgzSlider, vgzCheckBox, randomGlobalCheckBox);
        initializeTimers(templSlider, templCheckBox, randomGlobalCheckBox);
        initializeTimers(temphSlider, temphCheckBox, randomGlobalCheckBox);
        initializeTimers(tofSlider, tofCheckBox, randomGlobalCheckBox);
        initializeTimers(hSlider, hCheckBox, randomGlobalCheckBox);
        initializeTimers(batSlider, batCheckBox, randomGlobalCheckBox);
        initializeTimers(baroSlider, baroCheckBox, randomGlobalCheckBox);
        initializeTimers(agxSlider, agxCheckBox, randomGlobalCheckBox);
        initializeTimers(agySlider, agyCheckBox, randomGlobalCheckBox);
        initializeTimers(agzSlider, agzCheckBox, randomGlobalCheckBox);

        // Ajouter le panel au frame
        frame.add(panel);
        frame.setVisible(true);

        // Start telemetry sending timer
        Timer telemetryTimer = new Timer(100, e -> {
            String[] labels = {"mid", "x", "y", "z", "mpry", "pitch", "roll", "yaw", "vgx", "vgy", "vgz", "templ", "temph", "tof", "h", "bat", "baro", "agx", "agy", "agz"};
            String telemetryData = generateTelemetryData(
                labels,
                midSlider.getValue(), xSlider.getValue(), ySlider.getValue(), zSlider.getValue(), mprySlider.getValue(), pitchSlider.getValue(), rollSlider.getValue(), yawSlider.getValue(), vgxSlider.getValue(), vgySlider.getValue(), vgzSlider.getValue(), templSlider.getValue(), temphSlider.getValue(), tofSlider.getValue(), hSlider.getValue(), batSlider.getValue(), baroSlider.getValue(), agxSlider.getValue(), agySlider.getValue(), agzSlider.getValue()
            );
            try {
                sendTelemetryUDP(telemetryData, address, port_telemetry);
                System.out.println(ANSI_GREEN + "Telemetry sent: " + ANSI_RESET + telemetryData);
            } catch (IOException ex) {
                System.out.println(ANSI_RED + "Error sending telemetry: " + ANSI_RESET + ex.getMessage());
                ex.printStackTrace();
            }
        });
        telemetryTimer.start();
    }

    private static JSlider createSlider(JPanel panel, String label, int y) {
        JLabel sliderLabel = new JLabel(label);
        sliderLabel.setBounds(10, y, 100, 20);
        sliderLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(sliderLabel);

        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 10000, 0);
        slider.setBounds(120, y, 200, 50);
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(false);
        slider.setPaintLabels(false);
        panel.add(slider);

        return slider;
    }

    private static JCheckBox createCheckBox(JPanel panel, String label, int y) {
        JLabel checkBoxLabel = new JLabel(label);
        checkBoxLabel.setBounds(320, y, 60, 20);
        panel.add(checkBoxLabel);

        JCheckBox checkBox = new JCheckBox();
        checkBox.setBounds(370, y, 20, 20);
        panel.add(checkBox);

        return checkBox;
    }

    private static void initializeTimers(JSlider slider, JCheckBox randomCheckBox, JCheckBox randomGlobalCheckBox) {
        Timer timer = new Timer(50, e -> {
            if (randomGlobalCheckBox.isSelected() || randomCheckBox.isSelected()) {
                slider.setValue((int) (generateRandomValue() * 100));
            }
        });
        timer.start();
    
        // Désactiver/activer le timer selon la case globale
        randomGlobalCheckBox.addActionListener(e -> {
            if (randomGlobalCheckBox.isSelected()) {
                randomCheckBox.setSelected(true);
            } else {
                randomCheckBox.setSelected(false);
            }
        });
    
        randomCheckBox.addActionListener(e -> {
            if (!randomCheckBox.isSelected() && randomGlobalCheckBox.isSelected()) {
                randomCheckBox.setSelected(true);
            }
        });
    }

    private static double generateRandomValue() {
        Random random = new Random();
        double value = 100 * random.nextDouble();
        return Math.round(value * 100.0) / 100.0;
    }

    private static String generateTelemetryData(String[] labels, int... sliderValues) {
        StringBuilder telemetryData = new StringBuilder();
        for (int i = 0; i < sliderValues.length; i++) {
            double formattedValue = sliderValues[i] / 100.0;
            telemetryData.append(labels[i]).append(":").append(String.format("%.2f", formattedValue));
            if (i < sliderValues.length - 1) {
                telemetryData.append(";");
            }
        }
        return telemetryData.toString();
    }

    private static void sendTelemetryUDP(String telemetryData, String address, int port) throws IOException {
        byte[] data = telemetryData.getBytes();
        DatagramSocket socket = new DatagramSocket();
        InetAddress ipAddress = InetAddress.getByName(address);
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        socket.send(packet);
        socket.close();
    }
}