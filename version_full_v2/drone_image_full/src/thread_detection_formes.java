/**
 * -------------------------------------------------------------------
 * Nom du fichier : thread_detection_formes.java
 * Auteur         : BEAL JULIEN
 * Version        : 1.0
 * Date           : 03/02/2025
 * Description    : thread de detection de formes
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class thread_detection_formes extends Thread {

    private Mat frame , processedImage;
    private boolean detection;

    public thread_detection_formes(Mat frame , boolean detection) {
        this.frame = frame;
        this.detection = detection;
    }

    @Override
    public void run() {
        Mat grayImage = new Mat();
        Mat blurredImage = new Mat();
        Mat edges = new Mat();
        Mat processedImage = frame.clone();

        Thread.currentThread().setName("Detection de formes");

        while (true){
            if (detection == true) {
                
                // Convertir l'image en niveaux de gris
                Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

                // Appliquer un flou gaussien pour réduire le bruit
                Imgproc.GaussianBlur(grayImage, blurredImage, new Size(5, 5), 0);

                // Détection des contours avec Canny
                Imgproc.Canny(blurredImage, edges, 50, 150);

                // Trouver les contours
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                contours.removeIf(contour -> Imgproc.contourArea(contour) < 300); //Filtrer les petits contours

                for (MatOfPoint contour : contours) {
                    // Approximation des contours pour simplifier la forme
                    MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
                    double perimeter = Imgproc.arcLength(contour2f, true);
                    MatOfPoint2f approx = new MatOfPoint2f();
                    Imgproc.approxPolyDP(contour2f, approx, 0.02 * perimeter, true);

                    // Identifier la forme selon le nombre de sommets
                    int vertexCount = approx.toArray().length;
                    String shapeType = switch (vertexCount) {
                        case 3 -> "Triangle";
                        case 4 -> "Rectangle";
                        case 5 -> "Pentagone";
                        case 6 -> "Hexagone";
                        case 7 -> "Heptagone";
                        case 8 -> "Octogone";
                        default -> (vertexCount > 8 ) ? "cercle" :" ?";
                    };

                    // Dessiner les contours et afficher la forme détectée
                    Imgproc.drawContours(processedImage, List.of(contour), -1, new Scalar(0, 255, 0), 2);
                    if (!shapeType.equals(" ")) {
                        org.opencv.core.Point textPoint = approx.toArray()[0];
                        Imgproc.putText(processedImage, shapeType, textPoint, Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(255, 0, 0), 2);
                    }
                }

                // Libérer les ressources inutilisées
                grayImage.release();
                blurredImage.release();
                edges.release();
                hierarchy.release();

                detection = false;
            }
        }
    }
    public Mat getFrame() {
        return processedImage;
    }

    public void setFrame(Mat frame) {
        this.frame = frame;
        detection = true;
    }

    public boolean isFrame_process() {
        return detection;
    }

}



