
//###############################################################
//
//
//Auteur : Killian Cosson
//Titre : TelemetryGUI
//Version 1.0
//
//
//###############################################################
package util;


import javax.swing.*;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;



@SuppressWarnings("serial")
public class TelemetryClientGUI extends JFrame 
{
	// Composants graphiques
    private JTextArea telemetryTextArea;
    private JProgressBar distProgressBar,baroProgressBar,timeProgressBar,TemphProgressBar,AltProgressBar; // Declaration de la barre de progression
    private JButton pauseButton; // Bouton Pause/Reprendre
    private boolean isPaused = false; // Controle de l'etat de pause


    //#######################################################################
    // Constructeur : Configure l'interface graphique
    //
    // Constructeur pour configurer l'interface graphique
    public TelemetryClientGUI() 
    {
        // Configurer la fenetre principale
        setTitle("Supervision des donnees telemetriques");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centre la fenetre a l'ecran
        
        initUI(); // Initialisation de l'interface utilisateur

        
    }

    private void initUI() 
    {
    	// Creer une zone de texte pour afficher les donnees telemetriques
        this.telemetryTextArea = new JTextArea();
        this.telemetryTextArea.setEditable(false); // Empeche la modification
        this.telemetryTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        this.telemetryTextArea.setBackground(Color.BLACK); // Fond sombre pour type console
        this.telemetryTextArea.setForeground(Color.WHITE); // Texte clair

        // Ajouter une barre de defilement
        JScrollPane scrollPane = new JScrollPane(telemetryTextArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        // Creer un panneau pour contenir les barres de progression
        JPanel progressBarPanel = new JPanel();
        progressBarPanel.setLayout(new BoxLayout(progressBarPanel, BoxLayout.Y_AXIS)); // Empiler les barres verticalement

        // Creer les JProgressBars pour les differentes mesures
        this.distProgressBar = createProgressBar(Color.RED, "dist: 0",-5,100);
        this.baroProgressBar = createProgressBar(new Color(0, 128, 0), "baro: 0",0,2000);
        this.timeProgressBar = createProgressBar(Color.BLUE, "time: 0%",0,100);
        this.TemphProgressBar = createProgressBar(Color.ORANGE, "temph: 0",0,100);
        this.AltProgressBar = createProgressBar(new Color(128, 0, 128), "Alt: 0", 0, 300);
;
        
        // Ajouter les barres au panneau
        progressBarPanel.add(this.distProgressBar);
        progressBarPanel.add(this.baroProgressBar);
        progressBarPanel.add(this.timeProgressBar);
        progressBarPanel.add(this.TemphProgressBar);
        progressBarPanel.add(this.AltProgressBar);
        
        // Ajouter le panneau contenant les barres au haut de la fenetre
        getContentPane().add(progressBarPanel, BorderLayout.NORTH);
        
        // Ajouter un bouton pour quitter l'application
        JButton quitButton = new JButton("Quitter");
        quitButton.addActionListener(e -> System.exit(0)); // Ferme l'application
        getContentPane().add(quitButton, BorderLayout.SOUTH);
        
        // Ajouter un bouton pour mettre en pause et reprendre
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> togglePause()); // Interchange entre pause et reprendre
        getContentPane().add(pauseButton, BorderLayout.WEST);
    }
    

    //######################################################################
    //
    // Cree une barre de progression avec une couleur et un texte initial
    //
    private JProgressBar createProgressBar(Color color, String text, int min, int max) 
    {
        JProgressBar progressBar = new JProgressBar(min, max);
        progressBar.setStringPainted(true);
        progressBar.setForeground(color);
        progressBar.setBackground(Color.LIGHT_GRAY);
        progressBar.setString(text);
        return progressBar;
    }

    
    // Methode pour ajouter des donnees a la zone de texte
    public void appendTelemetryData(String data) {
        SwingUtilities.invokeLater(() -> {
        	this.telemetryTextArea.append(data + "\n");
        });
    }

    // Methode pour extraire des champs specifiques et afficher mid
    public String parseTelemetryData(String data) {
    	if (isPaused) return ""; // Si en pause, ne pas traiter les donnees
        StringBuilder parsedData = new StringBuilder();
        String[] keyValuePairs = data.split(";");
        for (String pair : keyValuePairs) 
        {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) 
            {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                // Afficher uniquement les champs souhaites (dist, baro, time)
                if (key.matches("dist|baro|time|temph|alt")) {
                    parsedData.append(key).append(": ").append(value).append("\n");
                    updateProgressBar(Double.parseDouble(value), key);
                }
            }
        }
        return parsedData.toString();
    }
    
    //###################################################################
    //
    // Met a jour la barre de progression associee a la cle donnee
    //
    private void updateProgressBar(double value, String key) 
    {
        int progress = (int) ((value + 100) / 2);
        if (isPaused) return; // Si en pause, ne pas mettre a jour
        SwingUtilities.invokeLater(() -> {
            switch (key) {
                case "dist":
                	distProgressBar.setValue(progress);
                	distProgressBar.setString("dist: " + value);
                    break;
                case "baro":
                    baroProgressBar.setValue(progress);
                    baroProgressBar.setString("baro: " + value);
                    break;
                case "time":
                    timeProgressBar.setValue(progress);
                    timeProgressBar.setString("time: " + progress + "%");
                    break;
                case "temph":
                	TemphProgressBar.setValue(progress);
                	TemphProgressBar.setString("temph: " + value);
                    break;
                case "alt":
                	AltProgressBar.setValue(progress);
                	AltProgressBar.setString("Alt: " + value);
                    break;
                default:
                    System.err.println("Cle inconnue pour mise a jour de la barre de progression.");
            }
        });
    }
    
    // Methode pour mettre en pause/reprendre
    private void togglePause() 
    {
        isPaused = !isPaused;
        this.pauseButton.setText(isPaused ? "Reprendre" : "Pause");
        this.telemetryTextArea.append(isPaused ? " Donnees en pause...\n" : " Reprise des donnees...\n");
    }

	// Methode pour envoyer des donnees UDP a une adresse IP specifique
    public static void sendUDPData(String data, String destinationIP, int port) 
	{
        try (DatagramSocket socket = new DatagramSocket()) 
		{
            InetAddress address = InetAddress.getByName(destinationIP);
            byte[] buffer = data.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);  // Envoi des donnees UDP
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

