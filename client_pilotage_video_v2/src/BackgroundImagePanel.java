import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Dimension;

public class BackgroundImagePanel extends JPanel {
    private Image backgroundImage;

    /**
     * Constructeur qui prend l'image de fond en paramètre
     * @param image L'image à utiliser comme fond
     */
    public BackgroundImagePanel(Image image) {
        this.backgroundImage = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Dessine l'image de fond en l'étirant pour remplir tout le panel
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    /**
     * Permet de changer l'image de fond
     * @param image La nouvelle image de fond
     */
    public void setBackgroundImage(Image image) {
        this.backgroundImage = image;
        repaint();
    }

    /**
     * Retourne l'image de fond actuelle
     * @return L'image de fond
     */
    public Image getBackgroundImage() {
        return backgroundImage;
    }

    @Override
    public Dimension getPreferredSize() {
        // Si une image de fond existe, utilise ses dimensions
        if (backgroundImage != null) {
            return new Dimension(
                backgroundImage.getWidth(this),
                backgroundImage.getHeight(this)
            );
        }
        return super.getPreferredSize();
    }
}