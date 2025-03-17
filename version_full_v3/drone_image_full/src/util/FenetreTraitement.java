/**
 * -------------------------------------------------------------------
 * Nom du fichier : FenetreTraitement.java
 * Auteur         : BEAL JULIEN
 * Version        : 2.0
 * Date           : 11/02/2025
 * Description    : Classe interface avec options de traitement
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

public class FenetreTraitement implements Runnable {
    private JFrame frame;
    private JPanel panel;
    private JPanel panelOptions;
    private JButton buttonForme;
    private JButton buttonContours;
    private JButton buttonFormeContours;
    private JButton buttonRien;
    private BufferedImage image;
    private int traitement = 0;
    private LocalDateTime lastModifiedTime = LocalDateTime.now();

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

        // Panel pour les boutons
        panelOptions = new JPanel();
        buttonRien = new JButton("rien");
        buttonContours = new JButton("contours");
        buttonForme = new JButton("formes");
        buttonFormeContours = new JButton("formes et contours");

        // Ajout des boutons au panel
        panelOptions.add(buttonRien); // traitement = 0
        panelOptions.add(buttonContours); // traitement = 1
        panelOptions.add(buttonForme); // traitement = 2
        panelOptions.add(buttonFormeContours); // traitement = 3

        // Ajout des composants à la fenêtre
        frame.add(panel, BorderLayout.CENTER);
        frame.add(panelOptions, BorderLayout.SOUTH);
        frame.setVisible(true);

        // Ajout des listeners pour les boutons
        buttonRien.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTraitement(0);
            }
        });

        buttonContours.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTraitement(1);
            }
        });

        buttonForme.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTraitement(2);
            }
        });

        buttonFormeContours.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTraitement(3);
            }
        });
    }

    // Permet de changer l'image affichée
    public void setImage(BufferedImage newImage) {
        this.image = newImage;
        panel.repaint();  // Rafraîchissement de l'affichage
    }

    // Permet de changer la valeur de traitement et met à jour le temps de la dernière modification
    private void setTraitement(int traitement) {
        this.traitement = traitement;
        this.lastModifiedTime = LocalDateTime.now();
    }

    // Retourne la valeur actuelle de traitement
    public int getTraitement() {
        return traitement;
    }

    // Retourne le temps de la dernière modification de traitement
    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    @Override
    public void run() {
        // La fenêtre s'affiche dès la création
    }
}
