import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class ImageReceiver {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("OpenCV library loaded successfully.");
    }
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static int receivedFrameCount = 0;
    private static int initialFrameCount = 0;
    private static long startTime = System.currentTimeMillis();
    private static int referencePort;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Ask for the reference port
        System.out.print("Enter the reference port: ");
        referencePort = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        int imagePort = referencePort;
        int infoPort = referencePort + 1;
        int telemetryPort = referencePort + 2;
        byte[] imageBuffer = new byte[65536]; // Buffer to hold incoming image data
        byte[] infoBuffer = new byte[65536]; // Buffer to hold incoming info data
        byte[] telemetryBuffer = new byte[65536]; // Buffer to hold incoming telemetry data

        ImageReceiver receiver = new ImageReceiver();

        Thread imageThread = new Thread(new UdpListener(imagePort, imageBuffer, receiver));
        Thread infoThread = new Thread(new UdpListener(infoPort, infoBuffer, receiver));
        Thread telemetryThread = new Thread(new UdpListener(telemetryPort, telemetryBuffer, receiver));

        imageThread.start();
        infoThread.start();
        telemetryThread.start();

        // Create a non-resizable window
        HighGui.namedWindow("Receiver", HighGui.WINDOW_NORMAL);

        // Main loop to keep the window open
        while (true) {
            // Wait 20 milliseconds to allow OpenCV to refresh the window
            if (HighGui.waitKey(1) == 27) { // 27 corresponds to the 'ESC' key
                break;
            }
        }
    }

    public synchronized void processPacket(DatagramPacket packet, int port) {
        try {
            if (port == referencePort) { // Replace with your actual image port
                processImagePacket(packet);
            } else if (port == referencePort+1) { // Replace with your actual info port
                processInfoPacket(packet);
            } else if (port == referencePort+2) { // Replace with your actual telemetry port
                processTelemetryPacket(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processImagePacket(DatagramPacket packet) throws IOException {
        System.out.println(ANSI_CYAN + "Image frame received." + ANSI_RESET);

        // Save the received data to a file
        String filePath = "received_image.jpg";
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(packet.getData(), 0, packet.getLength());
        }

        // Load the image using OpenCV
        Mat receivedImage = Imgcodecs.imread(filePath);

        if (!receivedImage.empty()) {
            // Resize the image for display at half size
            Mat displayFrameHalfSize = new Mat();
            Imgproc.resize(receivedImage, displayFrameHalfSize, new Size(receivedImage.width() / 2, receivedImage.height() / 2));

            // Display the resized image with the information
            HighGui.imshow("Receiver", displayFrameHalfSize);
        } else {
            System.out.println(ANSI_RED + "Failed to load the received image." + ANSI_RESET);
        }
    }

    private void processInfoPacket(DatagramPacket packet) {
        System.out.println(ANSI_YELLOW + "Info frame received." + ANSI_RESET);
        String infoText = new String(packet.getData(), 0, packet.getLength());

        // Extract frameCount, formattedFrequency, and sendTime from infoText
        String[] infoParts = infoText.split("; ");
        if (infoParts.length == 3) {
            int sentFrameCount = Integer.parseInt(infoParts[0].split(": ")[1]);
            double sentFrequency = Double.parseDouble(infoParts[1].split(": ")[1].replace(',', '.'));
            long sendTime = Long.parseLong(infoParts[2].split(": ")[1]);

            // Initialize the initial frame count on the first received frame
            if (initialFrameCount == -1) {
                initialFrameCount = sentFrameCount;
                startTime = System.currentTimeMillis();
            }

            // Increment the received frame count
            receivedFrameCount++;

            // Calculate the frequency of received frames
            long currentTime = System.currentTimeMillis();
            double duration = (currentTime - startTime) / 1000.0;
            double receivedFrequency = receivedFrameCount / duration;

            // Calculate the differences based on the initial frame count
            int frameCountDifference = (sentFrameCount - initialFrameCount) - receivedFrameCount;
            double frequencyDifference = sentFrequency - receivedFrequency;
            if (frequencyDifference < 0) {
                frequencyDifference = 0;
            }

            // Calculate the latency
            long latency = currentTime - sendTime;
            if (latency < 0) {
                latency = 0;
            }

            // Create the infoText strings
            String infoTextFrameCount = "Frame count: " + sentFrameCount;
            String infoTextFPS = "FPS: " + String.format("%.2f", sentFrequency);
            String recalculatedFrameCount = "Recalculated Frame count: " + receivedFrameCount;
            String recalculatedFPS = "Recalculated FPS: " + String.format("%.2f", receivedFrequency);
            String frameCountDiffText = "Frame count difference: " + frameCountDifference;
            String frequencyDiffText = "FPS difference: " + String.format("%.2f", frequencyDifference);
            String latencyText = "Latency: " + latency + " ms";

            // Display the received information in red on the image
            Mat displayFrameHalfSize = HighGui.getWindowImage("Receiver");
            if (displayFrameHalfSize != null) {
                Imgproc.putText(displayFrameHalfSize, infoTextFrameCount, new Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);
                Imgproc.putText(displayFrameHalfSize, infoTextFPS, new Point(10, 50), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);

                // Display the recalculated information in green on the image
                Imgproc.putText(displayFrameHalfSize, recalculatedFrameCount, new Point(10, 70), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                Imgproc.putText(displayFrameHalfSize, recalculatedFPS, new Point(10, 90), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);

                // Display the differences in blue on the image
                Imgproc.putText(displayFrameHalfSize, frameCountDiffText, new Point(10, 110), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 0, 0), 2);
                Imgproc.putText(displayFrameHalfSize, frequencyDiffText, new Point(10, 130), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 0, 0), 2);

                // Display the latency in yellow on the image
                Imgproc.putText(displayFrameHalfSize, latencyText, new Point(10, 150), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 255), 2);

                HighGui.imshow("Receiver", displayFrameHalfSize);
            }
        } else {
            System.out.println(ANSI_RED + "Received malformed info text: " + infoText + ANSI_RESET);
        }
    }

    private void processTelemetryPacket(DatagramPacket packet) {
        System.out.println(ANSI_PURPLE + "Telemetry frame received." + ANSI_RESET);
        String telemetryData = new String(packet.getData(), 0, packet.getLength()).trim();

        // Split telemetry data and display each on a new line
        String[] telemetryParts = telemetryData.split(";");
        Mat displayFrameHalfSize = HighGui.getWindowImage("Receiver");
        if (displayFrameHalfSize != null) {
            // Display the telemetry data on the right side of the image, spanning the entire height
            for (int i = 0; i < telemetryParts.length; i++) {
                Imgproc.putText(displayFrameHalfSize, telemetryParts[i], new Point(displayFrameHalfSize.width() - 200, 30 + (i * 20)), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);
            }

            HighGui.imshow("Receiver", displayFrameHalfSize);
        }
    }
}