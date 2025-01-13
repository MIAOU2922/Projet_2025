import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        // Initialiser le compteur d'images et le temps de départ
        int frameCount = 0;
        long startTime = System.currentTimeMillis();
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
            // Enregistrer le cadre en tant qu'image JPEG dans le dossier img avec un nom basé sur la date et l'heure
            String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date());
            Imgcodecs.imwrite("img/frame_" + ".jpg", displayFrame);
            frameCount++;
            long currentTime = System.currentTimeMillis();
            double duration = (currentTime - startTime) / 1000.0;
            double frequency = frameCount / duration;
            System.out.println("Nombre d'images enregistrées: " + frameCount + ", Fréquence: " + frequency + " images par seconde");
            // Attendre 10 millisecondes pour permettre à OpenCV de rafraîchir la fenêtre
            if (HighGui.waitKey(10) == 27) { // 27 correspond à la touche 'ESC'
                break;
            }
        }
        // Libérer l'objet de capture vidéo
        capture.release();
        // Fermer toutes les fenêtres OpenCV
        HighGui.destroyAllWindows();
    }
}