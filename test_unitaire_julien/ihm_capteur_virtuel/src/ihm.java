import javax.swing.*;
import java.util.Random;
import java.awt.event.*;

public class ihm {
    public static void main(String[] args) {
        // Créer le frame
        JFrame frame = new JFrame("IHM Capteur Virtuel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 700); // Augmenter la taille pour accueillir les nouveaux éléments
        
        // Créer le panel
        JPanel panel = new JPanel();
        panel.setLayout(null); // Utiliser un layout null pour positionner les composants manuellement

        // Ajouter le label "capteur virtuel"
        JLabel capteurVirtuelLabel = new JLabel("capteur virtuel");
        capteurVirtuelLabel.setBounds(10, 10, 100, 20); // Positionner le label
        panel.add(capteurVirtuelLabel);

        // Ajouter le label "random global"
        JLabel randomGlobalLabel = new JLabel("random global:");
        randomGlobalLabel.setBounds(285, 10, 100, 20); // Positionner le label
        panel.add(randomGlobalLabel);

        // Ajouter la case à cocher "random global"
        JCheckBox randomGlobalCheckBox = new JCheckBox();
        randomGlobalCheckBox.setBounds(370, 10, 20, 20); // Positionner la case à cocher
        panel.add(randomGlobalCheckBox);

        // Ajouter le label "T°:"
        JLabel Temperaturelabel = new JLabel("T°:");
        Temperaturelabel.setBounds(10, 40, 100, 20); // Positionner le label
        Temperaturelabel.setHorizontalAlignment(SwingConstants.RIGHT); // Aligner le texte à droite
        panel.add(Temperaturelabel);

        // Ajouter le slider
        JSlider Temperatureslider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
        Temperatureslider.setBounds(120, 40, 200, 50); // Positionner le slider
        Temperatureslider.setMajorTickSpacing(50);
        Temperatureslider.setMinorTickSpacing(10);
        Temperatureslider.setPaintTicks(true);
        Temperatureslider.setPaintLabels(true);
        panel.add(Temperatureslider);

        // Ajouter le label "random: "
        JLabel TemperaturerandomLabel = new JLabel("random: ");
        TemperaturerandomLabel.setBounds(320, 40, 60, 20); // Positionner le label
        panel.add(TemperaturerandomLabel);

        // Ajouter la case à cocher "random"
        JCheckBox TemperaturecheckBox = new JCheckBox();
        TemperaturecheckBox.setBounds(370, 40, 20, 20); // Positionner la case à cocher
        panel.add(TemperaturecheckBox);

        // Ajouter le label "Pression:"
        JLabel pressionLabel = new JLabel("Pression:");
        pressionLabel.setBounds(10, 100, 100, 20); // Positionner le label
        pressionLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Aligner le texte à droite
        panel.add(pressionLabel);

        // Ajouter le slider
        JSlider pressionSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
        pressionSlider.setBounds(120, 100, 200, 50); // Positionner le slider
        pressionSlider.setMajorTickSpacing(50);
        pressionSlider.setMinorTickSpacing(10);
        pressionSlider.setPaintTicks(true);
        pressionSlider.setPaintLabels(true);
        panel.add(pressionSlider);

        // Ajouter le label "random: "
        JLabel pressionRandomLabel = new JLabel("random: ");
        pressionRandomLabel.setBounds(320, 100, 60, 20); // Positionner le label
        panel.add(pressionRandomLabel);

        // Ajouter la case à cocher "random"
        JCheckBox pressionCheckBox = new JCheckBox();
        pressionCheckBox.setBounds(370, 100, 20, 20); // Positionner la case à cocher
        panel.add(pressionCheckBox);

        // Ajouter le label "Altitude:"
        JLabel altitudeLabel = new JLabel("Altitude:");
        altitudeLabel.setBounds(10, 160, 100, 20); // Positionner le label
        altitudeLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Aligner le texte à droite
        panel.add(altitudeLabel);

        // Ajouter le slider
        JSlider altitudeSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
        altitudeSlider.setBounds(120, 160, 200, 50); // Positionner le slider
        altitudeSlider.setMajorTickSpacing(50);
        altitudeSlider.setMinorTickSpacing(10);
        altitudeSlider.setPaintTicks(true);
        altitudeSlider.setPaintLabels(true);
        panel.add(altitudeSlider);

        // Ajouter le label "random: "
        JLabel altitudeRandomLabel = new JLabel("random: ");
        altitudeRandomLabel.setBounds(320, 160, 60, 20); // Positionner le label
        panel.add(altitudeRandomLabel);

        // Ajouter la case à cocher "random"
        JCheckBox altitudeCheckBox = new JCheckBox();
        altitudeCheckBox.setBounds(370, 160, 20, 20); // Positionner la case à cocher
        panel.add(altitudeCheckBox);

        // Ajouter le label "distances:"
        JLabel distancesLabel = new JLabel("distances:");
        distancesLabel.setBounds(10, 220, 100, 20); // Positionner le label
        distancesLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Aligner le texte à droite
        panel.add(distancesLabel);

        // Ajouter le slider
        JSlider distancesSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
        distancesSlider.setBounds(120, 220, 200, 50); // Positionner le slider
        distancesSlider.setMajorTickSpacing(50);
        distancesSlider.setMinorTickSpacing(10);
        distancesSlider.setPaintTicks(true);
        distancesSlider.setPaintLabels(true);
        panel.add(distancesSlider);

        // Ajouter le label "random: "
        JLabel distancesRandomLabel = new JLabel("random: ");
        distancesRandomLabel.setBounds(320, 220, 60, 20); // Positionner le label
        panel.add(distancesRandomLabel);

        // Ajouter la case à cocher "random"
        JCheckBox distancesCheckBox = new JCheckBox();
        distancesCheckBox.setBounds(370, 220, 20, 20); // Positionner la case à cocher
        panel.add(distancesCheckBox);

        // Ajouter le label "niveau batterie:"
        JLabel batterieLabel = new JLabel("niveau batterie:");
        batterieLabel.setBounds(10, 280, 100, 20); // Positionner le label
        batterieLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Aligner le texte à droite
        panel.add(batterieLabel);

        // Ajouter le slider
        JSlider batterieSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
        batterieSlider.setBounds(120, 280, 200, 50); // Positionner le slider
        batterieSlider.setMajorTickSpacing(50);
        batterieSlider.setMinorTickSpacing(10);
        batterieSlider.setPaintTicks(true);
        batterieSlider.setPaintLabels(true);
        panel.add(batterieSlider);

        // Ajouter le label "random: "
        JLabel batterieRandomLabel = new JLabel("random: ");
        batterieRandomLabel.setBounds(320, 280, 60, 20); // Positionner le label
        panel.add(batterieRandomLabel);

        // Ajouter la case à cocher "random"
        JCheckBox batterieCheckBox = new JCheckBox();
        batterieCheckBox.setBounds(370, 280, 20, 20); // Positionner la case à cocher
        panel.add(batterieCheckBox);

        // Ajouter le label "vitesse:"
        JLabel vitesseLabel = new JLabel("vitesse:");
        vitesseLabel.setBounds(10, 340, 100, 20); // Positionner le label
        vitesseLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Aligner le texte à droite
        panel.add(vitesseLabel);

        // Ajouter le slider
        JSlider vitesseSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
        vitesseSlider.setBounds(120, 340, 200, 50); // Positionner le slider
        vitesseSlider.setMajorTickSpacing(50);
        vitesseSlider.setMinorTickSpacing(10);
        vitesseSlider.setPaintTicks(true);
        vitesseSlider.setPaintLabels(true);
        panel.add(vitesseSlider);

        // Ajouter le label "random: "
        JLabel vitesseRandomLabel = new JLabel("random: ");
        vitesseRandomLabel.setBounds(320, 340, 60, 20); // Positionner le label
        panel.add(vitesseRandomLabel);

        // Ajouter la case à cocher "random"
        JCheckBox vitesseCheckBox = new JCheckBox();
        vitesseCheckBox.setBounds(370, 340, 20, 20); // Positionner la case à cocher
        panel.add(vitesseCheckBox);

        // Ajouter le label "Accéléromètre X:"
        JLabel accelXLabel = new JLabel("Accéléromètre X:");
        accelXLabel.setBounds(10, 400, 100, 20); // Positionner le label
        accelXLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Aligner le texte à droite
        panel.add(accelXLabel);

        // Ajouter le slider
        JSlider accelXSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
        accelXSlider.setBounds(120, 400, 200, 50); // Positionner le slider
        accelXSlider.setMajorTickSpacing(50);
        accelXSlider.setMinorTickSpacing(10);
        accelXSlider.setPaintTicks(true);
        accelXSlider.setPaintLabels(true);
        panel.add(accelXSlider);

        // Ajouter le label "random: "
        JLabel accelXRandomLabel = new JLabel("random: ");
        accelXRandomLabel.setBounds(320, 400, 60, 20); // Positionner le label
        panel.add(accelXRandomLabel);

        // Ajouter la case à cocher "random"
        JCheckBox accelXCheckBox = new JCheckBox();
        accelXCheckBox.setBounds(370, 400, 20, 20); // Positionner la case à cocher
        panel.add(accelXCheckBox);

        // Ajouter le label "Accéléromètre Y:"
        JLabel accelYLabel = new JLabel("Accéléromètre Y:");
        accelYLabel.setBounds(10, 460, 100, 20); // Positionner le label
        accelYLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Aligner le texte à droite
        panel.add(accelYLabel);

        // Ajouter le slider
        JSlider accelYSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
        accelYSlider.setBounds(120, 460, 200, 50); // Positionner le slider
        accelYSlider.setMajorTickSpacing(50);
        accelYSlider.setMinorTickSpacing(10);
        accelYSlider.setPaintTicks(true);
        accelYSlider.setPaintLabels(true);
        panel.add(accelYSlider);

        // Ajouter le label "random: "
        JLabel accelYRandomLabel = new JLabel("random: ");
        accelYRandomLabel.setBounds(320, 460, 60, 20); // Positionner le label
        panel.add(accelYRandomLabel);

        // Ajouter la case à cocher "random"
        JCheckBox accelYCheckBox = new JCheckBox();
        accelYCheckBox.setBounds(370, 460, 20, 20); // Positionner la case à cocher
        panel.add(accelYCheckBox);

        // Ajouter le label "Accéléromètre Z:"
        JLabel accelZLabel = new JLabel("Accéléromètre Z:");
        accelZLabel.setBounds(10, 520, 100, 20); // Positionner le label
        accelZLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Aligner le texte à droite
        panel.add(accelZLabel);

        // Ajouter le slider
        JSlider accelZSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
        accelZSlider.setBounds(120, 520, 200, 50); // Positionner le slider
        accelZSlider.setMajorTickSpacing(50);
        accelZSlider.setMinorTickSpacing(10);
        accelZSlider.setPaintTicks(true);
        accelZSlider.setPaintLabels(true);
        panel.add(accelZSlider);

        // Ajouter le label "random: "
        JLabel accelZRandomLabel = new JLabel("random: ");
        accelZRandomLabel.setBounds(320, 520, 60, 20); // Positionner le label
        panel.add(accelZRandomLabel);

        // Ajouter la case à cocher "random"
        JCheckBox accelZCheckBox = new JCheckBox();
        accelZCheckBox.setBounds(370, 520, 20, 20); // Positionner la case à cocher
        panel.add(accelZCheckBox);

        // Ajouter le panel au frame
        frame.add(panel);
        
        // Rendre le frame visible
        frame.setVisible(true);

        // Initialiser les timers pour chaque slider
        initializeTimers(Temperatureslider, TemperaturecheckBox);
        initializeTimers(pressionSlider, pressionCheckBox);
        initializeTimers(altitudeSlider, altitudeCheckBox);
        initializeTimers(distancesSlider, distancesCheckBox);
        initializeTimers(batterieSlider, batterieCheckBox);
        initializeTimers(vitesseSlider, vitesseCheckBox);
        initializeTimers(accelXSlider, accelXCheckBox);
        initializeTimers(accelYSlider, accelYCheckBox);
        initializeTimers(accelZSlider, accelZCheckBox);

        // Ajouter les listeners pour la case à cocher globale
        addGlobalCheckBoxListener(randomGlobalCheckBox, TemperaturecheckBox, pressionCheckBox, altitudeCheckBox, distancesCheckBox, batterieCheckBox, vitesseCheckBox, accelXCheckBox, accelYCheckBox, accelZCheckBox);
    }

    private static void initializeTimers(JSlider slider, JCheckBox checkBox) {
        Random random = new Random();
        Timer timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkBox.isSelected()) {
                    int value = random.nextInt(slider.getMaximum() + 1);
                    slider.setValue(value);
                }
            }
        });
        timer.start();
    }

    private static void addGlobalCheckBoxListener(JCheckBox globalCheckBox, JCheckBox... checkBoxes) {
        globalCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = globalCheckBox.isSelected();
                for (JCheckBox checkBox : checkBoxes) {
                    checkBox.setSelected(selected);
                }
            }
        });

        for (JCheckBox checkBox : checkBoxes) {
            checkBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (!checkBox.isSelected() && globalCheckBox.isSelected()) {
                        globalCheckBox.removeItemListener(this);
                        globalCheckBox.setSelected(false);
                        globalCheckBox.addItemListener(this);
                    }
                }
            });
        }
    }
}
