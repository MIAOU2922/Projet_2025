package test_unitaire_julien.test_camera_opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            System.out.println("Error: Camera not accessible");
            return;
        }

        Mat frame = new Mat();
        while (true) {
            if (camera.read(frame)) {
                HighGui.imshow("Camera Feed", frame);
                if (HighGui.waitKey(30) >= 0) {
                    break;
                }
            }
        }
        camera.release();
        HighGui.destroyAllWindows();
    }
}
