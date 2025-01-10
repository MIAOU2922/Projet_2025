import javax.swing.*;
import java.awt.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import java.util.Random;

public class Main {
    static {
        // Charger la bibliothèque native OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Créer la fenêtre principale
        JFrame frame = new JFrame("Combined IHM and OpenCV");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false); // Désactiver le redimensionnement

        // Créer le panneau IHM
        JPanel ihmPanel = createIHMPanel();
        frame.add(ihmPanel, BorderLayout.WEST);

        // Créer le panneau OpenCV
        JPanel opencvPanel = new JPanel();
        opencvPanel.setPreferredSize(new Dimension(600, 300)); // Définir une taille fixe
        frame.add(opencvPanel, BorderLayout.CENTER);

        // Démarrer la capture vidéo OpenCV dans un thread séparé
        new Thread(() -> startOpenCV(opencvPanel)).start();

        // Afficher la fenêtre
        frame.setVisible(true);
    }

    private static JPanel createIHMPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(400, 700));

        // Ajouter un label pour "Capteur virtuel"
        JLabel capteurVirtuelLabel = new JLabel("Capteur virtuel");
        capteurVirtuelLabel.setBounds(10, 10, 100, 20);
        panel.add(capteurVirtuelLabel);

        // Ajouter un label et une checkbox pour "Random global"
        JLabel randomGlobalLabel = new JLabel("Random global:");
        randomGlobalLabel.setBounds(285, 10, 100, 20);
        panel.add(randomGlobalLabel);

        JCheckBox randomGlobalCheckBox = new JCheckBox();
        randomGlobalCheckBox.setBounds(370, 10, 20, 20);
        panel.add(randomGlobalCheckBox);

        // Créer et ajouter les sliders, checkboxes et labels pour chaque capteur
        JSlider temperatureSlider = createSlider(panel, "T°:", 40);
        JCheckBox temperatureCheckBox = createCheckBox(panel, "random:", 40);
        JLabel temperatureLabel = createValueLabel(panel, 40);
        JSlider pressionSlider = createSlider(panel, "Pression:", 100);
        JCheckBox pressionCheckBox = createCheckBox(panel, "random:", 100);
        JLabel pressionLabel = createValueLabel(panel, 100);
        JSlider altitudeSlider = createSlider(panel, "Altitude:", 160);
        JCheckBox altitudeCheckBox = createCheckBox(panel, "random:", 160);
        JLabel altitudeLabel = createValueLabel(panel, 160);
        JSlider distancesSlider = createSlider(panel, "Distances:", 220);
        JCheckBox distancesCheckBox = createCheckBox(panel, "random:", 220);
        JLabel distancesLabel = createValueLabel(panel, 220);
        JSlider batterieSlider = createSlider(panel, "Niveau batterie:", 280);
        JCheckBox batterieCheckBox = createCheckBox(panel, "random:", 280);
        JLabel batterieLabel = createValueLabel(panel, 280);
        JSlider vitesseSlider = createSlider(panel, "Vitesse:", 340);
        JCheckBox vitesseCheckBox = createCheckBox(panel, "random:", 340);
        JLabel vitesseLabel = createValueLabel(panel, 340);
        JSlider accelXSlider = createSlider(panel, "Accéléromètre X:", 400);
        JCheckBox accelXCheckBox = createCheckBox(panel, "random:", 400);
        JLabel accelXLabel = createValueLabel(panel, 400);
        JSlider accelYSlider = createSlider(panel, "Accéléromètre Y:", 460);
        JCheckBox accelYCheckBox = createCheckBox(panel, "random:", 460);
        JLabel accelYLabel = createValueLabel(panel, 460);
        JSlider accelZSlider = createSlider(panel, "Accéléromètre Z:", 520);
        JCheckBox accelZCheckBox = createCheckBox(panel, "random:", 520);
        JLabel accelZLabel = createValueLabel(panel, 520);

        // Initialiser les timers pour chaque capteur
        initializeTimers(temperatureSlider, temperatureCheckBox, randomGlobalCheckBox, temperatureLabel);
        initializeTimers(pressionSlider, pressionCheckBox, randomGlobalCheckBox, pressionLabel);
        initializeTimers(altitudeSlider, altitudeCheckBox, randomGlobalCheckBox, altitudeLabel);
        initializeTimers(distancesSlider, distancesCheckBox, randomGlobalCheckBox, distancesLabel);
        initializeTimers(batterieSlider, batterieCheckBox, randomGlobalCheckBox, batterieLabel);
        initializeTimers(vitesseSlider, vitesseCheckBox, randomGlobalCheckBox, vitesseLabel);
        initializeTimers(accelXSlider, accelXCheckBox, randomGlobalCheckBox, accelXLabel);
        initializeTimers(accelYSlider, accelYCheckBox, randomGlobalCheckBox, accelYLabel);
        initializeTimers(accelZSlider, accelZCheckBox, randomGlobalCheckBox, accelZLabel);

        return panel;
    }

    private static JSlider createSlider(JPanel panel, String label, int y) {
        // Créer un label pour le slider
        JLabel sliderLabel = new JLabel(label);
        sliderLabel.setBounds(10, y, 100, 20);
        sliderLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(sliderLabel);

        // Créer le slider avec une échelle de 0 à 2000 (pour représenter des valeurs doubles)
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 2000, 0); // Échelle par 10 pour les valeurs doubles
        slider.setBounds(120, y, 200, 50);
        slider.setMajorTickSpacing(500);
        slider.setMinorTickSpacing(100);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        panel.add(slider);

        return slider;
    }

    private static JCheckBox createCheckBox(JPanel panel, String label, int y) {
        // Créer un label pour la checkbox
        JLabel checkBoxLabel = new JLabel(label);
        checkBoxLabel.setBounds(320, y, 60, 20);
        panel.add(checkBoxLabel);

        // Créer la checkbox
        JCheckBox checkBox = new JCheckBox();
        checkBox.setBounds(370, y, 20, 20);
        panel.add(checkBox);

        return checkBox;
    }

    private static JLabel createValueLabel(JPanel panel, int y) {
        // Créer un label pour afficher la valeur du slider
        JLabel valueLabel = new JLabel("0.0");
        valueLabel.setBounds(400, y, 50, 20);
        panel.add(valueLabel);

        return valueLabel;
    }

    private static void initializeTimers(JSlider slider, JCheckBox randomCheckBox, JCheckBox randomGlobalCheckBox, JLabel valueLabel) {
        Random random = new Random();

        // Créer un timer pour mettre à jour la valeur du slider et du label
        Timer timer = new Timer(100, e -> {
            // Si la checkbox globale ou locale est sélectionnée, générer une valeur aléatoire
            if (randomGlobalCheckBox.isSelected() || randomCheckBox.isSelected()) {
                int randomValue = random.nextInt(slider.getMaximum() + 1);
                slider.setValue(randomValue);
            }
            // Mettre à jour le label avec la valeur du slider (convertie en double)
            double sliderValue = slider.getValue() / 10.0; // Revenir à une valeur double
            valueLabel.setText(String.format("%.1f", sliderValue));
            System.err.println(sliderValue);
        });

        timer.start();

        // Ajouter des écouteurs pour les checkboxes
        randomGlobalCheckBox.addActionListener(e -> {
            if (randomGlobalCheckBox.isSelected()) {
                randomCheckBox.setSelected(true);
            } else {
                randomCheckBox.setSelected(false);
            }
        });

        randomCheckBox.addActionListener(e -> {
            if (!randomCheckBox.isSelected() && randomGlobalCheckBox.isSelected()) {
                randomGlobalCheckBox.setSelected(false);
            }
        });
    }

    private static void startOpenCV(JPanel panel) {
        // Ouvrir la capture vidéo
        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            System.out.println("Erreur : Impossible d'ouvrir la caméra vidéo");
            return;
        }

        Mat frame = new Mat();
        final Mat[] displayFrame = {new Mat()};
        int boxWidth = 600;
        int boxHeight = 300;
        int offsetX = 50; // Décalage vers la droite

        // Créer un timer pour mettre à jour le panneau avec les images de la caméra
        Timer timer = new Timer(33, e -> {
            capture.read(frame);
            if (frame.empty()) {
                System.out.println("Erreur : Impossible de capturer un cadre vidéo");
                return;
            }

            Imgproc.resize(frame, frame, new Size(boxWidth, boxHeight));
            displayFrame[0] = Mat.zeros(boxHeight, boxWidth + offsetX, frame.type());
            frame.copyTo(displayFrame[0].colRange(offsetX, offsetX + boxWidth));

            ImageIcon image = new ImageIcon(HighGui.toBufferedImage(displayFrame[0]));
            JLabel label = new JLabel(image);
            panel.removeAll();
            panel.add(label);
            panel.revalidate();
            panel.repaint();
        });
        timer.start();
    }
}
