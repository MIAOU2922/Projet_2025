import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ImageLayeringPanel extends JPanel {
    private BufferedImage img1, img2, img3;

    public ImageLayeringPanel() {
        try {
            // Charger les images
            img1 = ImageIO.read(new File("F:/BEAL_JULIEN_SN2/_projet_2025/302-ProspectionImplantationEoliennes/test_unitaire_julien/test_empilement_image_java/image1.png")); //
            img2 = ImageIO.read(new File("F:/BEAL_JULIEN_SN2/_projet_2025/302-ProspectionImplantationEoliennes/test_unitaire_julien/test_empilement_image_java/image2.png")); // 
            img3 = ImageIO.read(new File("F:/BEAL_JULIEN_SN2/_projet_2025/302-ProspectionImplantationEoliennes/test_unitaire_julien/test_empilement_image_java/image3.png")); //
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Dessiner la première image avec opacité 100%
        if (img1 != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Opacité 100%
            g2d.drawImage(img1, 0, 0, this);
        }

        // Dessiner la deuxième image avec opacité 100%
        if (img2 != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Opacité 100%
            g2d.drawImage(img2, 0, 0, this); 
        }

        // Dessiner la troisième image avec opacité 100%
        if (img3 != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Opacité 100%
            g2d.drawImage(img3, 0, 0, this); 
        }
    }

    public static void main(String[] args) {
        // Créer une fenêtre pour afficher le JPanel
        JFrame frame = new JFrame("Empilement d'Images avec Transparence");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Ajouter le panneau personnalisé avec les images
        ImageLayeringPanel panel = new ImageLayeringPanel();
        panel.setPreferredSize(new Dimension(400, 400)); // Taille de la fenêtre

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null); // Centrer la fenêtre
        frame.setVisible(true);
    }
}
