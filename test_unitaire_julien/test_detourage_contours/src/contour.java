import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;

public class contour {

    private static int boxWidth = 1280;
    private static int boxHeight = 720;

    public static void main(String[] args) {
        // Charger la bibliothèque OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Créer une fenêtre non redimensionnable
        HighGui.namedWindow("contour", HighGui.WINDOW_NORMAL);
        HighGui.resizeWindow("contour", boxWidth / 2, boxHeight / 2);

        // Variable pour stocker la dernière image valide
        Mat lastValidImage = null;

        // Boucle principale
        while (true) {
            // Charger l'image à partir du disque
            Mat image = Imgcodecs.imread("F:\\BEAL_JULIEN_SN2\\_projet_2025\\git\\test_unitaire_julien\\test_opencv_tcp\\img\\frame_quality70.jpg");

            if (image.empty()) {
                System.out.println("Erreur lors du chargement de l'image");
                if (lastValidImage != null) {
                    // Afficher la dernière image valide
                    HighGui.imshow("contour", lastValidImage);
                }
            } else {
                System.out.println("Image chargée");
                lastValidImage = image.clone(); // Stocker l'image d'origine comme dernière valide
                Mat processedImage = processImage(image); // Traiter l'image pour les contours
                HighGui.imshow("contour", processedImage); // Afficher l'image traitée
            }

            // Temporisation de 1 ms dans la boucle
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Attendre une touche pour quitter (Esc = 27)
            if (HighGui.waitKey(1) == 27) {
                break;
            }
        }

        // Libérer les ressources
        HighGui.destroyAllWindows();
    }

    /**
     * Traite une image pour détecter les contours et renvoyer l'image combinée avec des contours rouges.
     *
     * @param image L'image à traiter
     * @return L'image avec les contours rouges superposés
     */
    private static Mat processImage(Mat image) {
        // Convertir l'image en niveaux de gris
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Détection des contours avec l'algorithme Canny
        Mat edges = new Mat();
        Imgproc.Canny(grayImage, edges, 50, 150);

        // Créer une image avec des contours rouges
        Mat edgesRed = Mat.zeros(edges.size(), image.type()); // Initialiser une image noire
        for (int row = 0; row < edges.rows(); row++) {
            for (int col = 0; col < edges.cols(); col++) {
                if (edges.get(row, col)[0] > 0) {
                    // Définir la couleur rouge pour les pixels correspondant aux contours
                    edgesRed.put(row, col, new double[]{0, 0, 255});
                }
            }
        }

        // Superposer les contours rouges sur l'image d'origine
        Mat combinedImage = new Mat();
        Core.addWeighted(image, 1.0, edgesRed, 0.5, 0, combinedImage);

        return combinedImage; // Retourner l'image combinée
    }
}
