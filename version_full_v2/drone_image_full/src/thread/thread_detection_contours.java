/**
 * -------------------------------------------------------------------
 * Nom du fichier : thread_detection_contours.java
 * Auteur         : BEAL JULIEN
 * Version        : 2.0
 * Date           : 11/02/2025
 * Description    : Thread de détection de contours
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package thread;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import util.tempo;

public class thread_detection_contours extends Thread {

    private Mat frame, processedImage;
    private boolean detection;

    public thread_detection_contours(Mat frame, boolean detection) {
        this.frame = frame;
        this.detection = detection;
    }

    @Override
    public void run() {
        // Réutilisation des matrices pour éviter de les recréer à chaque appel
        Mat grayImage = new Mat();
        Mat flouImage = new Mat();
        Mat edges = new Mat();
        Mat dilatedEdges = new Mat();
        Mat edgesRed = new Mat();
        this.processedImage = new Mat();

        Thread.currentThread().setName("Detection de contours");

        while (true) {
            if (detection == true) {
                // Réduire la taille de l'image avant le traitement
                Mat resizedFrame = new Mat();
                Size reducedSize = new Size(frame.width() / 2, frame.height() / 2);
                Imgproc.resize(frame, resizedFrame, reducedSize);

                // Initialiser edgesRed avec la taille réduite
                edgesRed = Mat.zeros(resizedFrame.size(), resizedFrame.type());

                // Convertir l'image réduite en niveaux de gris
                Imgproc.cvtColor(resizedFrame, grayImage, Imgproc.COLOR_BGR2GRAY);

                // Appliquer un filtre bilatéral (lissage tout en préservant les contours)
                Imgproc.bilateralFilter(grayImage, flouImage, 9, 75, 75);

                // Détection des contours avec l'algorithme Canny
                int lowerThreshold = 50;
                int upperThreshold = 200;
                Imgproc.Canny(flouImage, edges, lowerThreshold, upperThreshold);

                // Appliquer la dilatation pour augmenter la taille des contours
                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)); // Taille 3x3
                Imgproc.dilate(edges, dilatedEdges, kernel);

                // Appliquer la couleur rouge (vectorisé)
                edgesRed.setTo(new Scalar(0, 0, 255), dilatedEdges); // Affecter rouge là où les contours sont présents

                // Réagrandir l'image traitée à sa taille d'origine
                Mat resizedEdgesRed = new Mat();
                Imgproc.resize(edgesRed, resizedEdgesRed, frame.size());

                // Superposer les contours rouges sur l'image d'origine
                Core.add(frame, resizedEdgesRed, this.processedImage);

                // Libérer les ressources inutilisées (libère explicitement la mémoire des objets temporaires)
                grayImage.release();
                flouImage.release();
                edges.release();
                dilatedEdges.release();
                edgesRed.release();
                resizedFrame.release();
                resizedEdgesRed.release();

                detection = false;
                //System.out.println("traitement contour fini");
            }
            new tempo(1);
        }
    }

    public Mat getFrame() {
        return this.processedImage;
    }

    public void setFrame(Mat frame) {
        this.frame = frame;
        detection = true;
    }

    public boolean isFrame_process() {
        return detection;
    }

}



