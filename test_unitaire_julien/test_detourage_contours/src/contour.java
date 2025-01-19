import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;

public class contour {

    public static void main(String[] args) {
        // Charger la bibliothèque OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Charger l'image à partir du disque
        Mat image = Imgcodecs.imread("G:\\logo_miaou.png");

        if (image.empty()) {
            System.out.println("Erreur lors du chargement de l'image");
            return;
        }

        // Convertir l'image en niveaux de gris
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Appliquer un flou pour réduire le bruit
        Mat blurredImage = new Mat();
        Imgproc.GaussianBlur(grayImage, blurredImage, new org.opencv.core.Size(5, 5), 1.5, 1.5);

        // Détection des contours avec l'algorithme Canny
        Mat edges = new Mat();
        Imgproc.Canny(blurredImage, edges, 100, 200);

        // Afficher l'image de contours
        HighGui.imshow("Contours", edges);
        HighGui.waitKey(0);

        // Sauvegarder l'image des contours
        Imgcodecs.imwrite("contours_output.jpg", edges);
    }
}
