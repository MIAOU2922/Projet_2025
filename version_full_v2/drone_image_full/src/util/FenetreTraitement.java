/**
 * -------------------------------------------------------------------
 * Nom du fichier : interface.java
 * Auteur         : BEAL JULIEN
 * Version        : 1.1
 * Date           : 04/02/2025
 * Description    : Classe interface avec options de traitement
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class FenetreTraitement implements Runnable {
    private JFrame frame;
    private JPanel panel;
    private JPanel panelOptions;
    private JCheckBox checkBoxContour;
    private JCheckBox checkBoxForme;
    private BufferedImage image;

    public FenetreTraitement(String titre, ImageIcon icon, int x, int y) {
        frame = new JFrame(titre);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 550);  // Augmentation de la hauteur pour ajouter les options
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        frame.setIconImage(icon.getImage());
        frame.setLocation(x, y);

        // Panel principal pour afficher l'image
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    g.drawImage(image, 0, 0, null);
                }
            }
        };

        // Panel pour les cases à cocher
        panelOptions = new JPanel();
        checkBoxContour = new JCheckBox("Contour");
        checkBoxForme = new JCheckBox("Forme");

        panelOptions.add(checkBoxContour);
        panelOptions.add(checkBoxForme);

        // Ajout des composants à la fenêtre
        frame.add(panel, BorderLayout.CENTER);
        frame.add(panelOptions, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    // Permet de changer l'image affichée
    public void setImage(BufferedImage newImage) {
        this.image = newImage;
        panel.repaint();  // Rafraîchissement de l'affichage
    }

   //methode pour recuperer l'etat du filtre
    public int getTraitement(){
        int nbr = 0;
        if (checkBoxContour.isSelected()){
            nbr=nbr+1;
        }
        if (checkBoxForme.isSelected()){
            nbr=nbr+2;
        }
        return nbr;
    }

    //methode pour déffinir l'etat du filtre
    public void setTraitement(int nbr){
        switch (nbr){
            case 0:
                checkBoxContour.setSelected(false);
                checkBoxForme.setSelected(false);
            break;
            case 1:
                checkBoxContour.setSelected(true);
                checkBoxForme.setSelected(false);
            break;
            case 2:
                checkBoxContour.setSelected(false);
                checkBoxForme.setSelected(true);
            break;
            case 3:
                checkBoxContour.setSelected(true);
                checkBoxForme.setSelected(true);
            break;
            default:
                checkBoxContour.setSelected(false);
                checkBoxForme.setSelected(false);
            break;
        }
    }

    @Override
    public void run() {
        // La fenêtre s'affiche dès la création
    }

    // Méthode pour lancer une nouvelle instance de la fenêtre
    public static void startNewInstance(String title, ImageIcon icon, int x, int y) {
        SwingUtilities.invokeLater(() -> new FenetreTraitement(title, icon, x, y));
    }
}
