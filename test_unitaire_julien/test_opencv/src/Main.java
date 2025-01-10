import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Open the default video camera
        VideoCapture capture = new VideoCapture(0);

        // Check if camera opened successfully
        if (!capture.isOpened()) {
            System.out.println("Error: Could not open video camera");
            return;
        }

        // Create a frame to hold the video
        Mat frame = new Mat();

        // Loop to continuously get frames from the video
        while (true) {
            // Capture a new frame
            capture.read(frame);

            // If the frame is empty, break the loop
            if (frame.empty()) {
                System.out.println("Error: Empty frame captured");
                break;
            }

            // Display the frame
            HighGui.imshow("Video Capture", frame);

            // Wait for 33 milliseconds and check if the user pressed the 'q' key
            if (HighGui.waitKey(33) == 'q') {
                break;
            }
        }

        // Release the video capture object
        capture.release();

        // Close all OpenCV windows
        HighGui.destroyAllWindows();
    }
}