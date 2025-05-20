/**
 * -------------------------------------------------------------------
 * Nom du fichier : Serveur_Char_GPIO.java
 * Auteur         : GRUBER NOE
 * Modification   : BEAL JULIEN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Serveur de commande du char
 * -------------------------------------------------------------------
 * © 2025 GRUBER NOE - Tous droits réservés
 */
package gpio;

import java.io.*;
import java.net.*;
import java.util.*;

import util.*;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.pwm.*;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.util.Console;

import main.drone_telemetrie;

public class Serveur_Char_GPIO extends Thread {
	
	private static int intPort = 1234;  					//port de communication 
	private static ServerSocket Serveur = null;				//Prise de communication cote Serveur
	private static Socket Client = null;					//Prise de communication cote Client
	private static InputStream FluxEntreeEthernet = null; 	//Flux d entree TCP  Serveur <- Client
	private static OutputStream FluxSortieEthernet = null;	//Flux de sortie TCP Serveur -> Client
	
	private static boolean ServeurUp = true; 				//Propriete d etat de fonctionnement du serveur
	private static boolean ClientConnect = false;			//Propriete d etat de Client connecte
	private static boolean StopAuto = true;
	private static double Longueur=0.0;
	private static double Largeur=0.0;
	private static double LargeurCam=0.0;
	private static int NombreDePassage=0;
	private static char charChoixAccuses = 'N';
	private static final int PIN_PWM_MG = 18, PIN_PWM_MD = 19, PIN_DIR_MG1 = 26, PIN_DIR_MG2 = 13, PIN_DIR_MD1 = 20, PIN_DIR_MD2 = 16; 
	
	private static DigitalOutput dirMG1;
	private static DigitalOutput dirMG2;
	private static Pwm pwmMG;
	private static PwmConfig pwmConfigMG;

	private static DigitalOutput dirMD1;
	private static DigitalOutput dirMD2;
	private static Pwm pwmMD;
	private static PwmConfig pwmConfigMD;
	
	private static double currentSpeedMG = 0.0;
	private static double currentSpeedMD = 0.0;

	private static double targetSpeedMG = 0.0;
	private static double targetSpeedMD = 0.0;
	private static final double ACCELERATION_STEP = 5.0; // Vitesse d'augmentation/diminution
	private static final double ROTATION_SENSITIVITY = 0.5;
	private static final int UPDATE_INTERVAL = 100; // Temps d'attente entre les mises a jour (ms)

	private static Thread motorControlThread;
	private static boolean running = false;
	
	private final drone_telemetrie drone;
	
	public Serveur_Char_GPIO (drone_telemetrie drone) {
		this.drone = drone;
	}
	    
	public void run() {
		try {
	    	// Initialisation de Pi4J
	        Context pi4j = Pi4J.newAutoContext();

	        // Récupération du fournisseur PWM (software)
		PwmProvider pwmProvider = pi4j.provider("pigpio-pwm");
	        try {
				

				// Configuration des broches PWM et digitales
				dirMG1 = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
						.id("DIR_MG1")
						.name("Direction moteur gauche 1")
						.address(PIN_DIR_MG1) // GPIO 25
						.shutdown(DigitalState.LOW)
						.initial(DigitalState.LOW)
						.build());
				System.out.println("dirMG1 initialise : " + (dirMG1 != null));

				dirMG2 = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
						.id("DIR_MG2")
						.name("Direction moteur gauche 2")
						.address(PIN_DIR_MG2) // GPIO 3
						.shutdown(DigitalState.LOW)
						.initial(DigitalState.LOW)
						.build());
				System.out.println("dirMG2 initialise : " + (dirMG2 != null));

				dirMD1 = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
						.id("DIR_MD1")
						.name("Direction moteur droit 1")
						.address(PIN_DIR_MD1) // GPIO 4
						.shutdown(DigitalState.LOW)
						.initial(DigitalState.LOW)
						.build());
				System.out.println("dirMD1 initialise : " + (dirMD1 != null));
				
				dirMD2 = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
						.id("DIR_MD2")
						.name("Direction moteur droit 2")
						.address(PIN_DIR_MD2) // GPIO 5
						.shutdown(DigitalState.LOW)
						.initial(DigitalState.LOW)
						.build());
				System.out.println("dirMD2 initialise : " + (dirMD2 != null));
				
				pwmConfigMG = Pwm.newConfigBuilder(pi4j)
						.id("PWM_MG")
						.name("PWM moteur gauche")
						.address(PIN_PWM_MG) // GPIO 1
						.pwmType(PwmType.SOFTWARE) // Mode Software
						.provider("pigpio-pwm")
						.frequency(1000)         // Fréquence : 1 kHz
						.dutyCycle(0)           // Rapport cyclique : 50%
						.build();
				pwmMG = pwmProvider.create(pwmConfigMG);
				System.out.println("pwmMG initialise : " + (pwmMG != null));

				pwmConfigMD = Pwm.newConfigBuilder(pi4j)
						.id("PWM_MD")
						.name("PWM moteur droit")
						.address(PIN_PWM_MD) // GPIO 24
						.pwmType(PwmType.SOFTWARE) // Mode Software
						.provider("pigpio-pwm")
						.frequency(1000)         // Fréquence : 1 kHz
						.dutyCycle(0)           // Rapport cyclique : 50%
						.build();
				pwmMD = pwmProvider.create(pwmConfigMD);
				System.out.println("pwmMD initialise : " + (pwmMD != null));
				
			 } catch (Exception e) {
				e.printStackTrace();
				System.out.println("probleme a l'initialisation");
			 }
	        
	        int intNbreCarLusEthernet = 0;
	    	String strRequeteRecueConvertie = null;
	    	String strRequeteRecue = null;
	    	byte byteTabRequeteRecue [] = new byte[1500];
	    	
			System.out.println("--------------------------------------------------------------------------------------------------");
			System.out.println("#                                      Serveur TEST Char                                         #");
			System.out.println("#                                                                                                #");
			System.out.println("# Auteur : Noe GRUBER                                                                            #");
			System.out.println("# Date : 13/01/2025                                                                              #");
			System.out.println("# Version : 1.0                                                                                  #");
			System.out.println("--------------------------------------------------------------------------------------------------\n");
			
			charChoixAccuses = 'O';
			System.out.println("Accuses de reception active.");
		
			// Boucle principale du serveur
			while( ServeurUp ) 
			{
		        System.out.println(".........................................................");
		        // initialisation des flux et socket
		        intNbreCarLusEthernet =0;
		    	FluxEntreeEthernet = null; 
		    	FluxSortieEthernet = null;
		    	Serveur = null;
		    	Client = null;
			
				try 
				{ 
					Serveur = new ServerSocket(1234); //# Creation d un socket de communication TCP en ecoute sur le port 1234   
					Temporisation(100);
				}
				catch(IOException e) 
				{
					System.out.println("Impossible d'ecouter le port: 1234 !!! \nFermeture du serveur");
					System.exit(0);
				}
				
				// Affichage des differentes interfaces TCP avec la methode GetHostNameAndIP()
				GetHostNameAndIP();
				System.out.println("#   	Port d ecoute : [ " + intPort+" ]                                                                 #");
				System.out.println("\nServeur en attente de connexion Client...");
				
				controleMoteur(0.0,0.0); 	// mise des moteur a 0 pour initialisation / perte de connexion
				pwmMG.off();
				pwmMD.off();

				Client = Serveur.accept(); 			//# Acceptation d une connexion Client et creation d'un socket client
				ClientConnect = true;
				
				System.out.println("Client connecte au serveur".toString());    	
			    // Creation d un flux de sortie avec le Client (emission de donnees serveur->Client)
				FluxSortieEthernet = Client.getOutputStream(); // Creation d un flux de sortie avec le Client sur le socket client(emission de donnees serveur->Client)
				
				// Envoi ou non d un accuse de reception au Client pour lui signifier qu il est connecte au serveur	
				if(EnvoyerAccuseReception("Connexion") == false)
				{
					System.out.println("Impossible de communiquer avec le Client !!! \nFermeture du serveur");
					System.exit(0);
				}
				 
				// Creation d un flux d entree avec le Client pour recevoir ses commandes (reception Serveur <- Client)
				FluxEntreeEthernet = Client.getInputStream(); //# Creation d un flux d entree avec le Client sur le socket client pour recevoir ses commandes (reception Serveur <- Client)
				Temporisation(100);
				
				// Boucle tant que le client est connecte et que nous n avons pas recu de demande de fermeture du serveur
				while(ClientConnect && ServeurUp)
				{
					try
					{	
						System.out.println("\nAttente de la Requete Client...");
						// Lecture des trames venant du Client reception, on recupere le nombre de caracteres lus et la trame est un tableau de bytes
						intNbreCarLusEthernet = FluxEntreeEthernet.read(byteTabRequeteRecue); //# Lecture de la trame recue sur le socket client
						
						// Envoi ou non d un accuse de reception au Client pour lui signifier que la commande a bien ete recue
						if(EnvoyerAccuseReception("OK") == false)
						{
							System.out.println("Impossible de communiquer avec le Client !!!");
							CleanSockets();
							ClientConnect = false;
							StopAuto=true;
						}
		   	
						if(intNbreCarLusEthernet > 0) // si des caracteres ont ete lus
						{
							//On convertit la trame recue en chaine de caracteres pour l afficher avec des carateres imprimables
							strRequeteRecueConvertie = ConvTrameCar(byteTabRequeteRecue,intNbreCarLusEthernet); 

					    	//-----------------------------------------------------------------------------------------------------------------
					    	//  Ici sont traitees les differentes requetes de commandes envoyees par le Client
					    	//-----------------------------------------------------------------------------------------------------------------
					    	
							// Le Client une requete de demande de fermeture a distance du serveur en envoyant : "Cmd : ShutDown"
							if( strRequeteRecueConvertie.contains("Cmd : ShutDown") )  //Si l on a recu ShutDown alors fermeture du serveur
							{
								System.out.println("_______________________________________________________________________________________________________________\n");
								System.out.println(strRequeteRecueConvertie);
								System.out.println("_______________________________________________________________________________________________________________\n");
								System.out.println("^ La Commande ci-dessus a ete recue ^\n");
								System.out.println("\nFermeture du serveur par le Client\n");
								
								controleMoteur(0.0,0.0); 	// mise des moteur a 0 pour fermeture
								pwmMG.off();
								pwmMD.off();
								
								CleanSockets();
								ClientConnect = false;
								ServeurUp = false;
								StopAuto=true;
							}
							else
							{	
								// Le Client envoi une requete de demande de deconnexion en envoyant : "Cmd : ClientDeConnecte"
								if(  strRequeteRecueConvertie.contains("Cmd : ClientDeConnecte") && (ClientConnect == true) )
								{
									System.out.println("_______________________________________________________________________________________________________________\n");
									System.out.println(strRequeteRecueConvertie);
									System.out.println("_______________________________________________________________________________________________________________\n");
									System.out.println("^ La Commande ci-dessus a ete recue ^\n");
									System.out.println("\nLe Client vient de se deconnecter !!!   Redemarrage du serveur en cours ....\n");
									
									CleanSockets();
									ClientConnect = false;
									StopAuto=true;
								}
								else	// Le Client envoi une trame de donnees
								{
									// Conversion de la trame de donnees pour un affichag
									strRequeteRecue = ConvTrameCar(byteTabRequeteRecue,intNbreCarLusEthernet);
									System.out.println("________________________________________________________________________________________________________________________________________________________________\n");
									System.out.println(strRequeteRecue); 
									System.out.println("_________________________________________________________________________________________________________________________________________________________________\n");
									System.out.println("^ La Trame de Donnees  ci-dessus a ete recue ^\n");
									
									if (strRequeteRecue.startsWith("|")&&(strRequeteRecue.endsWith("|")||strRequeteRecue.endsWith("|.")))
									{
										if (strRequeteRecue.endsWith("|")) { // enleve le | de fin de trame 
											strRequeteRecue = strRequeteRecue.substring(0, strRequeteRecue.length() - 1);
								        }
										else if (strRequeteRecue.endsWith("|.")) {
											strRequeteRecue = strRequeteRecue.substring(0, strRequeteRecue.length() - 2);
								        }
										
										strRequeteRecue = strRequeteRecue.substring(1); // enleve le | de debut de trame 
								        
										
										String[] parties = strRequeteRecue.split("/"); //decoupe la trame au /  
										double position_X;
										double position_Y;
										position_X=(Double.parseDouble(parties[0])); //convertire le string en double
										position_Y=(Double.parseDouble(parties[1]));
											
										System.out.println("________________________________________________________________________________________________________________________________________________________________\n");
										System.out.println("le joystick et en x: "+position_X+" ; y: "+position_Y);
										System.out.println("________________________________________________________________________________________________________________________________________________________________\n");
										
										controleMoteur(position_X,position_Y);
									}
									
									if (strRequeteRecue.contains("Dis")&&(strRequeteRecue.endsWith("|")||strRequeteRecue.endsWith("|.")))
									{
										StopAuto=false;
										if (strRequeteRecue.endsWith("|")) { // enleve le | de fin de trame 
											strRequeteRecue = strRequeteRecue.substring(0, strRequeteRecue.length() - 1);
								        }
										else if (strRequeteRecue.endsWith("|.")) {
											strRequeteRecue = strRequeteRecue.substring(0, strRequeteRecue.length() - 2);
								        }
										
										strRequeteRecue = strRequeteRecue.substring(4); // enleve le "Dis:" de debut de trame 
								        
										
										String[] parties = strRequeteRecue.split("/"); //decoupe la trame au /  
										
										Longueur=(Double.parseDouble(parties[0])); //convertire le string en double
										Largeur=(Double.parseDouble(parties[1]));
										LargeurCam=(Double.parseDouble(parties[2]));
										NombreDePassage=(int) Math.ceil(Largeur/LargeurCam);
										
										
										System.out.println("________________________________________________________________________________________________________________________________________________________________\n");
										System.out.println("le char vas patrouiller sur une Longueur de : "+Longueur+" m,");
										System.out.println("une Largeur de : "+Largeur+" m,");
										System.out.println("il fera "+NombreDePassage+" pasage,");
										System.out.println("________________________________________________________________________________________________________________________________________________________________\n");
										
										try {
											DistanceTracker tracker = new DistanceTracker(Double.valueOf(Longueur).longValue(), Double.valueOf(Largeur).longValue(), NombreDePassage, drone);
									        tracker.start();
										}catch (Exception e) {
										    e.printStackTrace();
										}
									}
									if (strRequeteRecue.contains("STOP")) {
										StopAuto=true;
									}
								}
							}
						}
					} 
					catch (IOException e1) // Si il y a eu une erreur de communication alors on considere que le client est deconnecte
					{
						System.out.println("Perte de la connexion !!!".toString());
						CleanSockets();
						ClientConnect = false;
					}
					new tempo(1);
				} 
			}
	        // Arret des GPIO
			 //pi4j.shutdown();
	        System.out.println("Programme termine.");
		} catch (Exception e) {
            e.printStackTrace();
        }
	    }
	    
	  //------------------------------------------------------------------------------------------------------
		
		//########################################################################// 
		//		Methode de fermeture des sockets TCP avec gestion des erreurs 	  //
		// 																	      // 																
		//########################################################################//
		public static void CleanSockets()
		{
			try
			{
				if(FluxEntreeEthernet!=null)
					FluxEntreeEthernet.close();
			} 
			catch (IOException e1) 
			{
				System.out.println("CleanSockets : Erreur sur FluxEntreeEthernet.close() !!!");
			}
			
	    	try 
	    	{
	    		if(FluxSortieEthernet!=null)
	    			FluxSortieEthernet.close();
			} 
	    	catch (IOException e2) 
	    	{
	    		System.out.println("CleanSockets : Erreur sur FluxSortieEthernet.close() !!!");
			}
	    	FluxEntreeEthernet = null; 
			FluxSortieEthernet = null;

			try 
			{
				if(Client!=null)
					Client.close();
			} 
			catch (IOException e3) 
			{
				System.out.println("CleanSockets : Erreur sur Client.close() !!!");
			}		
			Client=null;
			
			try 
			{
				if(Serveur!=null)
					Serveur.close();
			} 
			catch (IOException e4) 
			{
				System.out.println("CleanSockets : Erreur sur Serveur.close() !!!");
			}
			Serveur = null;
		}
		//------------------------------------------------------------------------------------------------------
		
		//##################################################################################// 
		// Methode d Enumeration des adaptateurs TCP actifs et des adresses ip associees    //
		//##################################################################################// 
		public static void GetHostNameAndIP()
		{
			System.out.println("\n#    Interface(s) reseau et adresse(s) disponible(s) sur ce serveur :                            #");
			// Creation d une liste pour recuperer les adresses IP des adaptateurs reseau
		    List<InetAddress> addrList = new ArrayList<InetAddress>();
		    // Utilisation d une enumeration pour lister les interfaces TCP reseau
		    Enumeration<NetworkInterface> interfaces = null;
		    try 
		    {
		    	// Stockage des informations de toutes les interfaces TCP reseau dans l enumeration
		        interfaces = NetworkInterface.getNetworkInterfaces(); 
		    } 
		    catch (SocketException e) 
		    {
		        System.out.println("Impossible de recuperer la liste des interfaces reseau !!! \nFermeture du serveur");
		        System.exit(0);
		    }

		    InetAddress localhost = null;

		    try
		    {
		    	// recuperation du nom du poste hote sur lequel fonctionne le serveur
		        localhost = InetAddress.getByName("127.0.0.1");
		    }
		    catch (UnknownHostException e) 
		    {
		    	System.out.println("Impossible de faire correspondre un nom DNS a l adresse IP Locale !!! \nFermeture du serveur");
		    	System.exit(0);
		    }

		    // recuperation des adresses de tous les adaptateur TCP reseau et affichage des informations sur les adaptateurs
		    while (interfaces.hasMoreElements()) 
		    {
		        NetworkInterface ifc = interfaces.nextElement();
		        Enumeration<InetAddress> addressesOfAnInterface = ifc.getInetAddresses();

		        while (addressesOfAnInterface.hasMoreElements()) 
		        {
		            InetAddress address = addressesOfAnInterface.nextElement();

		            if (!address.equals(localhost) && !address.toString().contains(":"))
		            {
		                addrList.add(address);
		                String strInfosAdaptateur;
		                strInfosAdaptateur = "#       [ Carte reseau : " + ifc.getDisplayName().toString() + " : " + address.getHostAddress()+ "  ]";
		                while(strInfosAdaptateur.length() < 97)
		                	strInfosAdaptateur = strInfosAdaptateur + " ";
		                System.out.println(strInfosAdaptateur+"#");
		            }
		        }
		    }
		}
		//------------------------------------------------------------------------------------------------------
		
		//############################################################################################################//    
		// Methode pour envoyer les accuses de reception au Client													  // 
		//						 									                                                  //
		// Les donnees a envoyer sont stockees dans un tableau de bytes									              //	
		// Accuses de reception utilises "Connexion" et "OK"														  //
		//############################################################################################################// 
		public static boolean EnvoyerAccuseReception(String strAccuseReception )
		{
	    	boolean EtatEnvoiAccuseReception = true;
	    	
			//Envoi d un accuse de reception au Client si accuses de reception active pour lui signifier qu il est connecte
			if( charChoixAccuses == 'O' || charChoixAccuses == 'o' ) 
			{ 	
		    	try 
		    	{
		    		// Envoi de l accuse de reception vers le Client
		    		System.out.println("Envoi de l accuse de reception au Client");
		    		FluxSortieEthernet.write(strAccuseReception.getBytes()); //# Ecrire (envoyer le tableau de bytes strAccuseReception) les accuses de reception sur le flux de sortie
					FluxSortieEthernet.flush();
				} 
		    	catch (IOException e) 
		    	{
		    		// Erreur lors de l envoi au Client
		    		System.out.println("Erreur lors de l envoi de l accuse de reception au Client !!!");
		    		EtatEnvoiAccuseReception=false;
				}
			}
			return EtatEnvoiAccuseReception;	// Retour si accuse de reception bien effectue ou non
		}
		
		
		//------------------------------------------------------------------------------------------------------
		
		//##############################################################################################//    
		// Methode pour afficher le contenu	de la trame	avec des caracters imprimables				    // 
		//							 									                               	//
		// les bytes sont convertis pour etre affiches les caracteres non imprimables sont remplaces    //
		// par un '.'													                                //	
		// Les reours chariot 0x0D 0x0A sont transformes en '\n'                                        //
		//##############################################################################################// 
		public static String ConvTrameCar(byte[] chainedeBytes, int taille)
		{
			String chaine="";
			byte[] Lebyte=new byte[1];
			int lebytetoint;
					
			for(int i=0; i < taille; i++)
			{
				if(chainedeBytes[i] >= 32 && chainedeBytes[i]<127)   //Cas des caracteres affichables
				{
					Lebyte[0]=chainedeBytes[i];
					lebytetoint = byteToUnsignedInt(Lebyte[0]);
					String monBytetoString = new String(Lebyte);
					chaine = chaine + monBytetoString; 
				}
				else
				{
					if(chainedeBytes[i] == 0x0D && chainedeBytes[i+1] == 0x0A)   //Cas du retour chariot /r/n
					{
						chaine = chaine + "\n";
						i++;
					}
					else														//Cas des carateres non affichables
					{
						Lebyte[0]=chainedeBytes[i];
						lebytetoint = byteToUnsignedInt(Lebyte[0]);
						if(lebytetoint < 16)
							chaine=chaine + ".";
						else
						{
							String monBytetoString = new String(Lebyte);
							chaine = chaine + monBytetoString;
						}
					}
				}
			}
			return chaine;
		}
		//------------------------------------------------------------------------------------------------------

		//##################################################################//    
		// Methode pour convertir un octet signe en entier non signe		// 												
		//##################################################################// 	
		public static int byteToUnsignedInt(byte b)
		{
	    	return (0x00 << 24 | b & 0xff);
		}
		//------------------------------------------------------------------------------------------------------

		//##################################################################//    
		// Methode pour faire une pause en ms                          		// 												
		//##################################################################// 	
		public static void Temporisation(int delai)
		{
			try 
			{
			  		// Temporisation 
			  		Thread.sleep(delai);
			} 
			catch (InterruptedException ex) 
			{
				//ex.printStackTrace();
			}
		}
		//------------------------------------------------------------------------------------------------------


	    public static void controleMoteur(Double x, Double y) {
	        
	    	// Arret immediat si demande
	        if (x == 0.0 && y == 0.0) {
	        	running = false;
	            if (motorControlThread != null && motorControlThread.isAlive()) {
	                motorControlThread.interrupt();
	            }
	            currentSpeedMG = 0.0;
	            currentSpeedMD = 0.0;
	            setDirectionAndSpeed(dirMG1, dirMG2, pwmMG, currentSpeedMG);
	            setDirectionAndSpeed(dirMD1, dirMD2, pwmMD, currentSpeedMD);
	            System.out.println("Arret immediat du robot pour des raisons de securite.");
	            return;
	        }

	        // Verification de la position dans la zone neutre
	        if ((x >= -15 && x <= 15) && (y >= -15 && y <= 15)) {
	        	targetSpeedMG = 0.0;
	            targetSpeedMD = 0.0;
	        } 
	        // Mouvement avant/arriere (y controle les deux moteurs de maniere egale)
	        else if (x >= -15 && x <= 15) {
	        	targetSpeedMG = y;
	            targetSpeedMD = y;
	        } 
	        // Rotation gauche/droite (x controle les moteurs en opposition)
	        else if (y >= -15 && y <= 15) {
	        	targetSpeedMG = x * ROTATION_SENSITIVITY;
	            targetSpeedMD = -x * ROTATION_SENSITIVITY;
	        } 
	        // Mouvement diagonal ou combinaison de rotation et deplacement
	        else {
	        	targetSpeedMG = (y + x * ROTATION_SENSITIVITY);
	            targetSpeedMD = (y - x * ROTATION_SENSITIVITY);
	            targetSpeedMG = Math.max(-100, Math.min(100, targetSpeedMG));
	            targetSpeedMD = Math.max(-100, Math.min(100, targetSpeedMD));
	        }
	        
	        // Demarrer le thread si ce n'est pas deja fait
	        if (motorControlThread == null || !motorControlThread.isAlive()) {
	            running = true;
	            motorControlThread = new Thread(() -> {
	                while (running) {
	                	if (Thread.currentThread().isInterrupted()) {
	                        break;
	                    }
	                	
	                    currentSpeedMG = ajusterProgressivement(currentSpeedMG, targetSpeedMG);
	                    currentSpeedMD = ajusterProgressivement(currentSpeedMD, targetSpeedMD);

	                    // Appliquer les nouvelles vitesses
	                    setDirectionAndSpeed(dirMG1, dirMG2, pwmMG, currentSpeedMG);
	                    setDirectionAndSpeed(dirMD1, dirMD2, pwmMD, currentSpeedMD);

	                    System.out.println("Vitesse moteur gauche : " + currentSpeedMG + "%, moteur droit : " + currentSpeedMD + "%.");
	                    
	                    if (currentSpeedMG == targetSpeedMG && currentSpeedMD == targetSpeedMD){
	                        running = false;
	                        break;
	                    }

	                    try {
	                        Thread.sleep(UPDATE_INTERVAL);
	                    } catch (InterruptedException e) {
	                        Thread.currentThread().interrupt();
	                        break;
	                    }
	                }
	            });
	            motorControlThread.start();
	        }
	    }
	    
	    /**
	     * Configure la direction et la vitesse d'un moteur.
	     * @param pin1 : Broche direction 1.
	     * @param pin2 : Broche direction 2.
	     * @param pwmPin : Broche PWM.
	     * @param speed : Vitesse (-100 a 100).
	     */
	    
	    private static void setDirectionAndSpeed(DigitalOutput  pin1, DigitalOutput  pin2, Pwm pwmPin, double speed) {
	        if (speed > 0) {
	            pin1.high();
	            pin2.low();
	        } else if (speed < 0) {
	            pin1.low();
	            pin2.high();
	        } else {
	            pin1.low();
	            pin2.low();
	        }
	        pwmPin.dutyCycle(Math.abs(speed));
	        pwmPin.on();
	    }
	    
	    private static double ajusterProgressivement(double current, double target) {
	        if (current < target) {
	            return Math.min(current + ACCELERATION_STEP, target);
	        } else if (current > target) {
	            return Math.max(current - ACCELERATION_STEP, target);
	        }
	        return current;
	    }
	    
	    public static boolean StopModeAuto() {
			
			return StopAuto;
		}
		
		public static double getVitesse() {
			Double Vitesse;
			Vitesse = Math.random() * (0.5 - 0.1) + 0.1;
			
			return Vitesse;
		}
}
