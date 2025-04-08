import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class VideoPanel extends JPanel {
    private BufferedImage currentFrame;
    private BufferedImage backgroundImage;

    public VideoPanel() {
        setLayout(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Dessine l'image de fond si elle existe
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        // Dessine l'image courante par-dessus si elle existe
        if (currentFrame != null) {
            g.drawImage(currentFrame, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public void updateFrame(BufferedImage frame) {
        this.currentFrame = frame;
        repaint();
    }

    public void setBackgroundImage(String imagePath) {
        try {
            backgroundImage = ImageIO.read(new File(imagePath));
            repaint();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'image de fond : " + e.getMessage());
        }
    }

    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
        repaint();
    }

    public void clearBackgroundImage() {
        this.backgroundImage = null;
        repaint();
    }
}
