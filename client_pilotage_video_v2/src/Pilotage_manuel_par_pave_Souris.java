import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.border.LineBorder;

import thread.*;
import util.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Pilotage_manuel_par_pave_Souris {

	// D�claration des composants de l'interface
	private JFrame frame;
    private VideoPanel videoPanel;
    private JLabel lblX, lblY, lblPort, lblIPServeur, labelVertical, labelHorizontal, lblLongueur, lblLargeur, lblLargeCam;
    private JButton btnConnexion, btnQuit, btnFermerServeur, btnEnvoiDistance, btnSTOP;
    private JTextField textField, textField_1;
    private JPanel joystickPanel;
    private JComboBox modeComboBox;
    private boolean EtatConnexion=false;
    private boolean EtatBoutonQuitter=false;
    private boolean EtatBoutonFermerServeur=false;
    private boolean EtatBoutonEnvoiDistance=false;
    private boolean EtatBoutonSTOP=false;
    private boolean haptiqueInitialise = false;
    private boolean haptiqueInitialiseM = false;
    private int x=0, y=0;
    private String ComboModePilote[]= {"S�lectionner un mode", "Joystick Virtuel", "Mode Automatique", "Robot Haptique"}; //liste des mode de guidage 
    private JSlider sliderHorizontal,  sliderVertical;
    private JTextField textFieldLongueur, textFieldLargeur, textFieldLargeCam;
    private String HaptiqueX="0", HaptiqueY="0", HaptiqueZ="0";
    private static ThreadJNIFalcon monThreadJNIFalcon;
    private static client Client_video;


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
    	Pilotage_manuel_par_pave_Souris window = new Pilotage_manuel_par_pave_Souris();
    	EventQueue.invokeLater(() -> {
            try {
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    	//new VideoStreamServer(window.videoPanel, 55001).start(); //lancement du Thread de lecteur du flux vid�o
    	Client_envoit_PMPS monClient = new Client_envoit_PMPS(window);
        monClient.start(); //lancement du Thread de communication au serveur (char)
        monThreadJNIFalcon = new ThreadJNIFalcon(window);
        monThreadJNIFalcon.start();


    }

    /**
     * Create the application.
     * @wbp.parser.entryPoint
     */
    public Pilotage_manuel_par_pave_Souris() {

        
        Client_video = new client();
        Client_video.start(); // Demarre le thread

        Thread refreshThread = new Thread() {
            @Override 
            public void run() {
                while (true) {
                    try {
                        videoPanel.setBackgroundImage(Client_video.getCurrentImage());
                        videoPanel.repaint(); // Refresh the video panel
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        };
        // Start the thread
        refreshThread.setName("VideoRefreshThread");
        refreshThread.start();


        initialize();
    }
    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
    	frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        videoPanel = new VideoPanel();
        frame.setContentPane(videoPanel);
        frame.getContentPane().setLayout(null);
        
        modeComboBox = new JComboBox(ComboModePilote);
        modeComboBox.setBounds(458, 34, 200, 30);
        modeComboBox.addActionListener(e -> updateMode());
        frame.getContentPane().add(modeComboBox);

        // Taille de l'�cran
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screenSize.height;
        int width = screenSize.width;
        
        // Panel to simulate joystick area with a background image
        ImageIcon backgroundImage = new ImageIcon("images/image_position_XY.png");
        joystickPanel = new BackgroundImagePanel(backgroundImage.getImage());
        joystickPanel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
        joystickPanel.setBounds(50, height - 275, 200, 200);
        frame.getContentPane().add(joystickPanel);
        joystickPanel.setLayout(null);

        // JLabel for X-coordinate
        lblX = new JLabel("X: 0");
        lblX.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblX.setBounds(270, height - 265, 100, 20);
        frame.getContentPane().add(lblX);

        // JLabel for Y-coordinate
        lblY = new JLabel("Y: 0");
        lblY.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblY.setBounds(270, height - 235, 100, 20);
        frame.getContentPane().add(lblY);
        
        lblPort = new JLabel("le port de communication :");
        lblPort.setBounds(12, 12, 176, 14);
        frame.getContentPane().add(lblPort);
        
        lblIPServeur = new JLabel("adresse du serveur :");
        lblIPServeur.setBounds(12, 38, 123, 14);
        frame.getContentPane().add(lblIPServeur);
        
        textField = new JTextField();
        textField.setText("1234");
        textField.setBounds(180, 9, 86, 20);
        frame.getContentPane().add(textField);
        textField.setColumns(10);
        
        textField_1 = new JTextField();
        textField_1.setText("172.29.1.26");
        textField_1.setColumns(10);
        textField_1.setBounds(143, 38, 123, 20);
        frame.getContentPane().add(textField_1);
        
        // Bouton de connexion
        btnConnexion = new JButton("Connexion");
        btnConnexion.setBounds(278, 8, 123, 23);
        btnConnexion.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				setConnexion();
			}
		});
        frame.getContentPane().add(btnConnexion);
        
        
        // Bouton de fermeture et Quitter
        btnQuit = new JButton("Quitter");
        btnQuit.setBounds(413, 8, 89, 23);
        btnQuit.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				EtatBoutonQuitter=true;
				VideoStreamServer.stopServer();
			}
		});
        frame.getContentPane().add(btnQuit);
        
        btnFermerServeur = new JButton("Fermer le Serveur");
        btnFermerServeur.setBounds(278, 38, 168, 23);
        btnFermerServeur.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				EtatBoutonFermerServeur=true;
				VideoStreamServer.stopServer();
			}
		});
        frame.getContentPane().add(btnFermerServeur);
        
        
        
        sliderHorizontal = new JSlider(JSlider.HORIZONTAL, -100, 100, 0);
        sliderHorizontal.setBounds(50, 75, 16, frame.getHeight() - 200);
        videoPanel.add(sliderHorizontal);
        
        labelHorizontal = new JLabel("Y : 0");
        labelHorizontal.setBounds(200, frame.getHeight() - 130, 150, 30);
        
        sliderVertical = new JSlider(JSlider.VERTICAL, -100, 100, 0);
        sliderVertical.setBounds(50, frame.getHeight() - 100, frame.getWidth() - 100, 16);
        videoPanel.add(sliderVertical);
        
        labelVertical = new JLabel("X : 0");
    	labelVertical.setBounds(80, frame.getHeight()/2+75, 100, 30);
        
        sliderVertical.addChangeListener(e -> labelVertical.setText("Z : " + sliderVertical.getValue()));
        sliderHorizontal.addChangeListener(e -> labelHorizontal.setText("X : " + sliderHorizontal.getValue()));
        frame.getContentPane().add(labelVertical);
        frame.getContentPane().add(labelHorizontal);
        
        lblLongueur = new JLabel("Longeur en m :");
        lblLongueur.setBounds(12, 70, 123, 16);
        videoPanel.add(lblLongueur);
        
        textFieldLongueur = new JTextField();
        textFieldLongueur.setBounds(153, 68, 114, 20);
        videoPanel.add(textFieldLongueur);
        textFieldLongueur.setColumns(10);
        
        lblLargeur = new JLabel("Largeur en m :");
        lblLargeur.setBounds(12, 102, 123, 16);
        videoPanel.add(lblLargeur);
        
        textFieldLargeur = new JTextField();
        textFieldLargeur.setBounds(152, 100, 114, 20);
        videoPanel.add(textFieldLargeur);
        textFieldLargeur.setColumns(10);
        
        btnEnvoiDistance = new JButton("Envoi des Distance");
        btnEnvoiDistance.setBounds(361, 97, 141, 26);
        btnEnvoiDistance.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				EtatBoutonEnvoiDistance=true;
			}
		});
        videoPanel.add(btnEnvoiDistance);
        
        btnSTOP = new JButton("STOP");
        btnSTOP.setBounds(522, 97, 141, 26);
        btnSTOP.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				EtatBoutonSTOP=true;
			}
		});
        videoPanel.add(btnSTOP);
        
        lblLargeCam= new JLabel("largeur prise en charge par la cam�ra");
        lblLargeCam.setBounds(12, 134, 222, 16);
        videoPanel.add(lblLargeCam);
        
        textFieldLargeCam = new JTextField();
        textFieldLargeCam.setBounds(238, 132, 114, 20);
        videoPanel.add(textFieldLargeCam);
        textFieldLargeCam.setColumns(10);
        
        // Add mouse listener for joystick simulation
        joystickPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                updateCoordinates(e.getX(), e.getY(), joystickPanel.getWidth(), joystickPanel.getHeight());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                resetCoordinates();
            }
        });
        
        btnQuit.setVisible(false);
        btnFermerServeur.setVisible(false);
        joystickPanel.setVisible(false);
        lblX.setVisible(false);
        lblY.setVisible(false);
        modeComboBox.setVisible(false);
        sliderHorizontal.setVisible(false);
        sliderVertical.setVisible(false);
        labelHorizontal.setVisible(false);
        labelVertical.setVisible(false);
        lblLongueur.setVisible(false);
        textFieldLongueur.setVisible(false);
        lblLargeur.setVisible(false);
        textFieldLargeur.setVisible(false);
        btnEnvoiDistance.setVisible(false);
        btnSTOP.setVisible(false);
        lblLargeCam.setVisible(false);
        textFieldLargeCam.setVisible(false);

        joystickPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateCoordinates(e.getX(), e.getY(), joystickPanel.getWidth(), joystickPanel.getHeight());
            }
        });
        // Set the background image for the video panel
        try {
            videoPanel.setBackgroundImage(Client_video.getCurrentImage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        frame.repaint();
    }

    /**
     * Update the coordinates based on mouse position.
     */
    private void updateCoordinates(int mouseX, int mouseY, int panelWidth, int panelHeight) {
        // Normalize to -100 to 100 range
    	int hereX;
    	int hereY;
    	hereX = (int) ((mouseX - panelWidth / 2.0) / (panelWidth / 2.0) * 100);
    	hereY = (int) ((panelHeight / 2.0 - mouseY) / (panelHeight / 2.0) * 100);

        // Clamp values to -100 to 100
        hereX = Math.max(-100, Math.min(100, hereX));
        hereY = Math.max(-100, Math.min(100, hereY));

        // Update labels
        lblX.setText("X: " + hereX);
        lblY.setText("Y: " + hereY);
        
        //if (hereX>=x+10||hereX<=x-10)
        //{
        	x=hereX;
       // }
        //if (hereY>=y+10||hereY<=y-10)
        //{
        	y=hereY;
        //}
    }
    
    /**
     * Reset the coordinates to 0 when the mouse is released.
     */
    private void resetCoordinates() {
        lblX.setText("X: 0");
        lblY.setText("Y: 0");
        x=0;
        y=0;
    }
    
    private void setConnexion() {
    	if (EtatConnexion==false)
    	{
        	EtatConnexion=true;
    		btnConnexion.setText("Deconnexion");
    		btnQuit.setVisible(true);
            btnFermerServeur.setVisible(true);
            modeComboBox.setVisible(true);
            textField.setEnabled(false);
            textField_1.setEnabled(false);
            modeComboBox.setSelectedItem("S�lectionner un mode");
    	}
    	else 
    	{
    		EtatConnexion=false;
    		btnConnexion.setText("Connexion");
    		btnQuit.setVisible(false);
            btnFermerServeur.setVisible(false);
            modeComboBox.setVisible(false);
            textField.setEnabled(true);
            textField_1.setEnabled(true);
    	}
    }
    
    private void updateMode() {
        String selectedMode = (String) modeComboBox.getSelectedItem();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screenSize.height;

        // V�rifie la disponibilit� du robot haptique
        if ("Robot Haptique".equals(selectedMode) && (monThreadJNIFalcon == null || !monThreadJNIFalcon.isHaptiqueDisponible())) {
            // Revenir � l'option par d�faut
            modeComboBox.setSelectedIndex(0);

            // Message d'alerte
            JOptionPane.showMessageDialog(frame,
                "Le robot haptique n'est pas connect�.\nVeuillez le brancher pour activer ce mode.",
                "Robot Haptique Indisponible",
                JOptionPane.WARNING_MESSAGE
            );
            return; // Emp�che d'ex�cuter la suite de updateMode()
        }
        if ("Joystick Virtuel".equals(selectedMode)) {
        	lblX.setBounds(270, height - 265, 100, 20);
        	lblY.setBounds(270, height - 235, 100, 20);
        	joystickPanel.setBounds(50, height - 275, 200, 200);
        	joystickPanel.setVisible(true);
        	lblX.setVisible(true);
        	lblY.setVisible(true);
        	sliderHorizontal.setVisible(false);
        	sliderVertical.setVisible(false);
        	labelHorizontal.setVisible(false);
            labelVertical.setVisible(false);
            lblLongueur.setVisible(false);
            textFieldLongueur.setVisible(false);
            lblLargeur.setVisible(false);
            textFieldLargeur.setVisible(false);
            btnEnvoiDistance.setVisible(false);
            btnSTOP.setVisible(false);
            lblLargeCam.setVisible(false);
            textFieldLargeCam.setVisible(false);
            haptiqueInitialise = false;
            haptiqueInitialiseM=false;
        } else if ("Robot Haptique".equals(selectedMode)) {
        	joystickPanel.setVisible(false);
        	lblX.setVisible(false);
        	lblY.setVisible(false);
        	lblLongueur.setVisible(false);
            textFieldLongueur.setVisible(false);
            lblLargeur.setVisible(false);
            textFieldLargeur.setVisible(false);
            btnEnvoiDistance.setVisible(false);
            btnSTOP.setVisible(false);
            lblLargeCam.setVisible(false);
            textFieldLargeCam.setVisible(false);
        	sliderHorizontal.setVisible(true);
        	sliderVertical.setVisible(true);
        	labelHorizontal.setVisible(true);
            labelVertical.setVisible(true);
        	sliderHorizontal.setBounds(50, frame.getHeight() - 100, frame.getWidth() - 100, 16);
        	sliderVertical.setBounds(50, 75, 16, frame.getHeight() - 200);
        	labelVertical.setBounds(80, frame.getHeight()/2-40, 100, 30);
        	labelHorizontal.setBounds(frame.getWidth()/2-15, frame.getHeight() - 130, 150, 30);
        } else if ("Mode Automatique".equals(selectedMode)) {
        	joystickPanel.setVisible(false);
        	lblX.setVisible(false);
        	lblY.setVisible(false);
        	sliderHorizontal.setVisible(false);
        	sliderVertical.setVisible(false);
        	labelHorizontal.setVisible(false);
            labelVertical.setVisible(false);
            lblLongueur.setVisible(true);
            textFieldLongueur.setVisible(true);
            lblLargeur.setVisible(true);
            textFieldLargeur.setVisible(true);
            btnEnvoiDistance.setVisible(true);
            btnSTOP.setVisible(true);
            lblLargeCam.setVisible(true);
            textFieldLargeCam.setVisible(true);
            haptiqueInitialise = false;
            haptiqueInitialiseM=false;
        } else {
        	joystickPanel.setVisible(false);
        	lblX.setVisible(false);
        	lblY.setVisible(false);
        	sliderHorizontal.setVisible(false);
        	sliderVertical.setVisible(false);
        	labelHorizontal.setVisible(false);
            labelVertical.setVisible(false);
            lblLongueur.setVisible(false);
            textFieldLongueur.setVisible(false);
            lblLargeur.setVisible(false);
            textFieldLargeur.setVisible(false);
            btnEnvoiDistance.setVisible(false);
            btnSTOP.setVisible(false);
            lblLargeCam.setVisible(false);
            textFieldLargeCam.setVisible(false);
            haptiqueInitialise = false;
            haptiqueInitialiseM=false;
        }
    }
    
    public boolean getEtatConnexion() 
	{
		return(EtatConnexion);
	}
    
    public boolean getEtatEnvoiDistance() 
   	{
   		return(EtatBoutonEnvoiDistance);
   	}
    
    public boolean getEtatSTOP() 
   	{
   		return(EtatBoutonSTOP);
   	}
    
    public void setEtatSTOP() 
   	{
    	EtatBoutonSTOP=false;
   	}
    
    public void setEtatEnvoiDistance() 
   	{
   		EtatBoutonEnvoiDistance=false;
   	}
    
    public String getLargeCam() 
   	{
   		return(textFieldLargeCam.getText());
   	}
    
    public String getLong() 
	{
		return(textFieldLongueur.getText());
	}
    
    public String getLarge() 
	{
		return(textFieldLargeur.getText());
	}
    
    public String getIP() 
	{
		return(textField_1.getText());
	}
	
	public String getPort() 
	{
		return(textField.getText());
	}
	
	public boolean getEtatBoutonQuitter()
	{
		boolean booleanEtatQuitter;
		
		booleanEtatQuitter = EtatBoutonQuitter;
		return(booleanEtatQuitter);
	}
	
	public boolean getEtatBoutonFermerServeur()
	{
		boolean booleanEtatFermerServeur;
		
		booleanEtatFermerServeur = EtatBoutonFermerServeur;
		return(booleanEtatFermerServeur);
	}
	
	public String getPosX()
	{
		String selectedMode = (String) modeComboBox.getSelectedItem();
		if ("Robot Haptique".equals(selectedMode)&&monThreadJNIFalcon.getDHDButton()==1) {
			if (haptiqueInitialise) {
				return HaptiqueX;
			} else {
				if (!haptiqueInitialiseM){
					System.out.println("veyer mettre le robot Haptique en position 0x 0z");
					haptiqueInitialiseM=true;
				}
				return "0"; // ou une valeur par d�faut tant que non initialis�
			}
		}else {
		String PosX;
		PosX=String.valueOf(x);
		return PosX;
		}
	}
	
	public String getPosY()
	{
		String selectedMode = (String) modeComboBox.getSelectedItem();
		if ("Robot Haptique".equals(selectedMode)&&monThreadJNIFalcon.getDHDButton()==1) {
			if (haptiqueInitialise) {
				return HaptiqueZ;
			} else {
				if (!haptiqueInitialiseM){
					System.out.println("veyer mettre le robot Haptique en position 0x 0z");
					haptiqueInitialiseM=true;
				}
				return "0"; // ou une valeur par d�faut tant que non initialis�
			}
		}else {
		String PosY;
		PosY=String.valueOf(y);
		return PosY;
		}
	}
	
	public void setVision(double y, double x, double z) 
	{
		if ("Robot Haptique".equals((String) modeComboBox.getSelectedItem())) {
			HaptiqueX=String.valueOf((int) Math.max(-100, Math.min(100, x*1000)));
			HaptiqueY=String.valueOf((int) Math.max(-100, Math.min(100, y*1000)));
			HaptiqueZ=String.valueOf((int) Math.max(-100, Math.min(100, z*1000)));
			
			// V�rification de l'initialisation du haptique
			int hX = Integer.parseInt(HaptiqueX);
			int hY = Integer.parseInt(HaptiqueY);
			int hZ = Integer.parseInt(HaptiqueZ);
			if (!haptiqueInitialise && hX >= -10 && hX <= 10 && hZ >= -10 && hZ <= 10) {
				haptiqueInitialise = true;
				System.out.println("Haptique initialis� !");
			}
		}
		sliderHorizontal.setValue((int) Math.round(x*1000));
		sliderVertical.setValue((int) Math.round(z*1000));
	}
	
	public void FermerIHM()
	{
		frame.dispose();
	}
	
}