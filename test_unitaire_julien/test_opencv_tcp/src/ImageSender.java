import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class ImageSender {
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

    // Variable declarations
    private static int boxWidth = 1280;
    private static int boxHeight = 720;
    private static int offsetX = 0;
    private static int frameCount = 0;
    private static long startTime = System.currentTimeMillis();
    private static String imgDirPath = "img";
    private static String filePath = imgDirPath + "/frame_quality70.jpg";

    private static String address;
    private static int port_image;
    private static int port_info;

    private static double frequency, duration;
    private static long currentTime , sendTime;
    private static String formattedFrequency, infoTextFPS, infoTextFrameCount, infoText;

    private static Queue<Long> frameTimes = new LinkedList<>();
    private static final int MAX_FRAMES = 100;

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

        port_image = referencePort;
        port_info = referencePort + 1;

        if (isBroadcast) {
            address = "172.29.255.255"; // Replace with your network's broadcast address
        } else {
            System.out.print("Enter the receiver's IP address: ");
            address = scanner.nextLine().trim();
        }

        // Create the img directory if it doesn't exist
        File imgDir = new File(imgDirPath);
        if (!imgDir.exists()) {
            imgDir.mkdir();
        }

        // Open the default video camera
        VideoCapture capture = new VideoCapture(0);
        System.out.println("Attempting to open the video camera...");
        if (!capture.isOpened()) {
            System.out.println(ANSI_RED +"Error: Unable to open the video camera" + ANSI_RESET);
            return;
        }
        System.out.println(ANSI_GREEN + "Video camera opened successfully."+ ANSI_RESET);

        // Create a frame to hold the video
        Mat frame = new Mat();
        Mat displayFrame = new Mat();

        // Create a non-resizable window
        HighGui.namedWindow("Sender", HighGui.WINDOW_NORMAL);
        HighGui.resizeWindow("Sender", boxWidth / 2, boxHeight / 2);

        // Loop to continuously get frames from the video
        while (true) {
            // Capture a new frame
            capture.read(frame);
            if (frame.empty()) {
                System.out.println(ANSI_RED + "Error: Unable to capture a video frame"+ ANSI_RESET);
                break;
            }

            // Resize the frame to 16:9 format
            Imgproc.resize(frame, frame, new Size(boxWidth, boxHeight));
            displayFrame = Mat.zeros(boxHeight, boxWidth + offsetX, frame.type());
            frame.copyTo(displayFrame.colRange(offsetX, offsetX + boxWidth));

            // Resize the image for display at half size
            Mat displayFrameHalfSize = new Mat();
            Imgproc.resize(displayFrame, displayFrameHalfSize, new Size(boxWidth / 2, boxHeight / 2));

            // Calculate the frame rate
            frameCount++;
            currentTime = System.currentTimeMillis();
            duration = (currentTime - startTime) / 1000.0;
            frequency = frameCount / duration;
            formattedFrequency = String.format("%.2f", frequency);

            // Display information in green on the image
            infoTextFrameCount = "Frame count: " + frameCount;
            infoTextFPS = "FPS: " + formattedFrequency;
            Imgproc.putText(displayFrameHalfSize, infoTextFrameCount, new Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
            Imgproc.putText(displayFrameHalfSize, infoTextFPS, new Point(10, 50), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);

            // Display the frame
            HighGui.imshow("Sender", displayFrameHalfSize);

            // Resize the image before saving
            Mat resizedFrame = new Mat();
            Imgproc.resize(displayFrame, resizedFrame, new Size(boxWidth, boxHeight));
            Imgcodecs.imwrite(filePath, resizedFrame, new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 70));

            // Send the image via UDP
            try {
                sendImageUDP(filePath, address, port_image);
                System.out.println(ANSI_YELLOW +"Image sent via UDP"+ANSI_RESET);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Create the infoText string by concatenating the information
            sendTime = System.currentTimeMillis();
            infoText = infoTextFrameCount + "; " + infoTextFPS + "; " + "Send time: " + sendTime;

            // Send the information via UDP
            try {
                sendInfoUDP(infoText, address, port_info);
                System.out.println(ANSI_BLUE+"Information sent via UDP"+ANSI_RESET);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //System.out.println("Number of frames recorded: " + frameCount + ", Frequency: " + formattedFrequency + " frames per second");

            // Wait 20 milliseconds to allow OpenCV to refresh the window
            if (HighGui.waitKey(1) == 27) { // 27 corresponds to the 'ESC' key
                break;
            }
        }

        // Release the video capture object
        capture.release();
        // Close all OpenCV windows
        HighGui.destroyAllWindows();
    }

    private static void sendImageUDP(String filePath, String address, int port) throws IOException {
        File imgFile = new File(filePath);
        byte[] imgData = new byte[(int) imgFile.length()];
        try (FileInputStream fis = new FileInputStream(imgFile)) {
            fis.read(imgData);
        }
        DatagramSocket socket = new DatagramSocket();
        InetAddress ipAddress = InetAddress.getByName(address);
        DatagramPacket packet = new DatagramPacket(imgData, imgData.length, ipAddress, port);
        socket.send(packet);
        socket.close();
    }

    private static void sendInfoUDP(String info, String address, int port) throws IOException {
        byte[] infoData = info.getBytes();
        DatagramSocket socket = new DatagramSocket();
        InetAddress ipAddress = InetAddress.getByName(address);
        DatagramPacket packet = new DatagramPacket(infoData, infoData.length, ipAddress, port);
        socket.send(packet);
        socket.close();
    }
}