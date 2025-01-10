import javax.swing.*;
import java.util.Random;

public class ihm2 {
    public static void main(String[] args) {
        // Créer le frame
        JFrame frame = new JFrame("IHM Capteur Virtuel 2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 700); 
        
        // Créer le panel
        JPanel panel = new JPanel();
        panel.setLayout(null);

        // Ajouter le label "capteur virtuel"
        JLabel capteurVirtuelLabel = new JLabel("Capteur virtuel");
        capteurVirtuelLabel.setBounds(10, 10, 100, 20);
        panel.add(capteurVirtuelLabel);

        // Ajouter le label et la case "random global"
        JLabel randomGlobalLabel = new JLabel("Random global:");
        randomGlobalLabel.setBounds(285, 10, 100, 20);
        panel.add(randomGlobalLabel);

        JCheckBox randomGlobalCheckBox = new JCheckBox();
        randomGlobalCheckBox.setBounds(370, 10, 20, 20);
        panel.add(randomGlobalCheckBox);

        // Ajouter sliders et contrôles associés
        JSlider temperatureSlider = createSlider(panel, "T°:", 40);
        JCheckBox temperatureCheckBox = createCheckBox(panel, "random:", 40);
        JSlider pressionSlider = createSlider(panel, "Pression:", 100);
        JCheckBox pressionCheckBox = createCheckBox(panel, "random:", 100);
        JSlider altitudeSlider = createSlider(panel, "Altitude:", 160);
        JCheckBox altitudeCheckBox = createCheckBox(panel, "random:", 160);
        JSlider distancesSlider = createSlider(panel, "Distances:", 220);
        JCheckBox distancesCheckBox = createCheckBox(panel, "random:", 220);
        JSlider batterieSlider = createSlider(panel, "Niveau batterie:", 280);
        JCheckBox batterieCheckBox = createCheckBox(panel, "random:", 280);
        JSlider vitesseSlider = createSlider(panel, "Vitesse:", 340);
        JCheckBox vitesseCheckBox = createCheckBox(panel, "random:", 340);

        // Ajouter des timers
        initializeTimers(temperatureSlider, temperatureCheckBox, randomGlobalCheckBox);
        initializeTimers(pressionSlider, pressionCheckBox, randomGlobalCheckBox);
        initializeTimers(altitudeSlider, altitudeCheckBox, randomGlobalCheckBox);
        initializeTimers(distancesSlider, distancesCheckBox, randomGlobalCheckBox);
        initializeTimers(batterieSlider, batterieCheckBox, randomGlobalCheckBox);
        initializeTimers(vitesseSlider, vitesseCheckBox, randomGlobalCheckBox);

        // Ajouter le panel au frame
        frame.add(panel);
        frame.setVisible(true);
    }

    private static JSlider createSlider(JPanel panel, String label, int y) {
        JLabel sliderLabel = new JLabel(label);
        sliderLabel.setBounds(10, y, 100, 20);
        sliderLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(sliderLabel);

        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
        slider.setBounds(120, y, 200, 50);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
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

    private static void initializeTimers(JSlider slider, JCheckBox randomCheckBox, JCheckBox randomGlobalCheckBox) {
        Random random = new Random();

        Timer timer = new Timer(500, e -> {
            if (randomGlobalCheckBox.isSelected() || randomCheckBox.isSelected()) {
                int randomValue = random.nextInt(slider.getMaximum() + 1);
                slider.setValue(randomValue);
            }
        });

        timer.start();

        // Désactiver/activer le timer selon la case globale
        randomGlobalCheckBox.addActionListener(e -> {
            if (randomGlobalCheckBox.isSelected()) {
                randomCheckBox.setSelected(true);
            }
        });
    }
}
