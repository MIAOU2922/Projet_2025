import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("OpenCV library loaded successfully.");
    }
    public static void main(String[] args) {
        // Ouvrir la caméra vidéo par défaut
        VideoCapture capture = new VideoCapture(0);
        System.out.println("Attempting to open the video camera...");
        // Vérifier si la caméra s'est ouverte avec succès
        if (!capture.isOpened()) {
            System.out.println("Erreur : Impossible d'ouvrir la caméra vidéo");
            return;
        }
        System.out.println("Video camera opened successfully.");
        // Créer un cadre pour contenir la vidéo
        Mat frame = new Mat();
        Mat displayFrame = new Mat();
        // Taille de la boîte 16:9
        int boxWidth = 640;
        int boxHeight = 360;
        int offsetX = 250;
        // Boucle pour obtenir continuellement des cadres de la vidéo
        while (true) {
            // Capturer un nouveau cadre
            capture.read(frame);
            // Si le cadre est vide, sortir de la boucle
            if (frame.empty()) {
                System.out.println("Erreur : Impossible de capturer un cadre vidéo");
                break;
            }
            // Redimensionner le cadre au format 16:9
            Imgproc.resize(frame, frame, new Size(boxWidth, boxHeight));
            // Créer une nouvelle image avec un espace de 150 pixels à gauche
            displayFrame = Mat.zeros(boxHeight, boxWidth + offsetX, frame.type());
            // Copier le cadre redimensionné dans la nouvelle image
            frame.copyTo(displayFrame.colRange(offsetX, offsetX + boxWidth));
            // Afficher le cadre
            HighGui.imshow("Capture Vidéo", displayFrame);
            // Attendre 33 millisecondes et vérifier si l'utilisateur a appuyé sur la touche 'q'
            if (HighGui.waitKey(33) == 'q') {
                break;
            }
        }
        // Libérer l'objet de capture vidéo
        capture.release();
        // Fermer toutes les fenêtres OpenCV
        HighGui.destroyAllWindows();
    }
}