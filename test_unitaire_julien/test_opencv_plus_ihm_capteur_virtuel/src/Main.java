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
        panel.setPreferredSize(new Dimension(500, 700)); // Largeur ajustée

        JLabel capteurVirtuelLabel = new JLabel("Capteur virtuel");
        capteurVirtuelLabel.setBounds(10, 10, 100, 20);
        panel.add(capteurVirtuelLabel);

        JLabel randomGlobalLabel = new JLabel("Random global:");
        randomGlobalLabel.setBounds(285, 10, 100, 20);
        panel.add(randomGlobalLabel);

        JCheckBox randomGlobalCheckBox = new JCheckBox();
        randomGlobalCheckBox.setBounds(370, 10, 20, 20);
        panel.add(randomGlobalCheckBox);

        int yPosition = 40; // Position verticale initiale
        int ySpacing = 70; // Espacement entre les lignes

        // Ajout des sliders, checkboxes et labels
        for (int i = 0; i < 9; i++) {
            JSlider slider = createSlider(panel, "Capteur " + (i + 1) + ":", yPosition);
            JCheckBox checkBox = createCheckBox(panel, "random:", yPosition);
            JLabel valueLabel = createValueLabel(panel, yPosition);
            initializeTimers(slider, checkBox, randomGlobalCheckBox, valueLabel);
            yPosition += ySpacing;
        }

        return panel;
    }

    private static JSlider createSlider(JPanel panel, String label, int y) {
        JLabel sliderLabel = new JLabel(label);
        sliderLabel.setBounds(10, y, 100, 20);
        sliderLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(sliderLabel);

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
        JLabel checkBoxLabel = new JLabel(label);
        checkBoxLabel.setBounds(320, y, 60, 20);
        panel.add(checkBoxLabel);

        JCheckBox checkBox = new JCheckBox();
        checkBox.setBounds(370, y, 20, 20);
        panel.add(checkBox);

        return checkBox;
    }

    private static JLabel createValueLabel(JPanel panel, int y) {
        JLabel valueLabel = new JLabel("0.0");
        valueLabel.setBounds(430, y, 50, 20); // Décalage ajusté pour éviter le chevauchement
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Alignement à droite
        valueLabel.setBorder(BorderFactory.createLineBorder(Color.RED)); // Pour visualiser les dimensions
        panel.add(valueLabel);

        return valueLabel;
    }

    private static void initializeTimers(JSlider slider, JCheckBox randomCheckBox, JCheckBox randomGlobalCheckBox, JLabel valueLabel) {
        Random random = new Random();

        Timer timer = new Timer(100, e -> {
            if (randomGlobalCheckBox.isSelected() || randomCheckBox.isSelected()) {
                int randomValue = random.nextInt(slider.getMaximum() + 1);
                slider.setValue(randomValue);
            }
            double sliderValue = slider.getValue() / 10.0; // Revenir à une valeur double
            valueLabel.setText(String.format("%.1f", sliderValue));
        });

        timer.start();

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
