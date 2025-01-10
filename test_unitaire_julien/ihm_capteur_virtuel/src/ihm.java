import java.awt.*;
import javax.swing.*;

public class ihm {
    public static void main(String[] args) {
        // Créer le frame
        JFrame frame = new JFrame("IHM Capteur Virtuel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 100);
        
        // Créer le panel
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        // Ajouter le label "T°:"
        JLabel label = new JLabel("T°:");
        panel.add(label);

        // Ajouter le slider
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        panel.add(slider);

        // Ajouter le label "random: "
        JLabel randomLabel = new JLabel("random: ");
        panel.add(randomLabel);

        // Ajouter la case à cocher
        JCheckBox checkBox = new JCheckBox();
        panel.add(checkBox);

        // Ajouter le panel au frame
        frame.add(panel);
        
        // Rendre le frame visible
        frame.setVisible(true);
    }
}
