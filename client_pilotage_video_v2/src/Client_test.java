
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Enumeration;

import clavier.In;

/* Exemple de Client TCP en java Port de communication 1234 
 * Etablit une connexion TCP avec un serveur envoie de commande ou de donnees et attend un accusé de réception du serveur
 *
 * Auteur : Gruber Noe
 * date : 14/01/2025
 * version 1.0
 */

public class Client_test {
	private static String strAdresseServeur; 				//adresse du serveur
	private static int intPort = 1234;  					//numero de port du serveur compatible
	private static Socket SocketClient = null;				//Prise de communication cote Client
	private static InputStream FluxEntreeEthernet = null; 	//Flux d entree TCP  Client <- Serveur
	private static OutputStream FluxSortieEthernet = null;	//Flux de sortie TCP Client -> Serveur
	
	private static boolean boolReboucler = true;
	private static boolean ServeurUp; 				//Propriete d etat de fonctionnement du serveur
	
	public static void main(String[] args) 
	{
		while(boolReboucler)
		{
			ServeurUp = true;
	    	String RequeteEnvoyee = null;
	    	// initialisation des flux et socket
	    	FluxEntreeEthernet = null; 
	    	FluxSortieEthernet = null;
	    	SocketClient = null;
	    	
			System.out.println("--------------------------------------------------------------------------------------------------");
			System.out.println("#                                      Client  TEST TCP                                          #");
			System.out.println("#                                                                                                #");
			System.out.println("# Auteur : Gruber Noe                                                                            #");
			System.out.println("# Date : 14/01/2025                                                                              #");
			System.out.println("# Version : 1.0                                                                                  #");
			System.out.println("--------------------------------------------------------------------------------------------------\n");
			
			// Saisie port et adresse du serveur
			System.out.println("Entrez le port de communication du serveur : ");
			intPort = In.readInteger();
			System.out.println("Entrez l adresse du serveur : ");
			strAdresseServeur = In.readString();
		/*	intPort=1234;
			strAdresseServeur = "127.0.0.1";
	*/
			try 
			{	// Creation du socket de communication avec le serveur
				SocketClient = new Socket(strAdresseServeur, intPort);	//# Creation d un socket de communication entre le Serveur et le Client avec l adresse et le port du serveur
				try 
				{	// Creation du flux de communication en sortie Client -> Serveur
					FluxSortieEthernet = SocketClient.getOutputStream();	//# Creation du flux de sortie Client -> Serveur
					try 
					{	// Creation du flux de communication en entree Client <- Serveur
						FluxEntreeEthernet = SocketClient.getInputStream();	//# Creation du flux d entree Client <- Serveur
						// Attente de l accuse de reception de connexion du serveur
						if(AttendreAccuseReception("Connexion") == true)
							System.out.println("Serveur connecte");						
						else
						{
							CleanSockets();
							ServeurUp = false;
						}
	
						//Boucle d envoi des commandes ou donnees vers le serveur et reception des accuses de reception du serveur. 
						while(ServeurUp) 
						{
							// Saisie des commandes ou donnees a envoyer au serveur
							System.out.println("\nSaisissez les donnees ou commandes a envoyer sous forme x/y ou Cmd : ClientDeConnecte pour se deconnecter");
							RequeteEnvoyee = In.readString();
							
							try 
							{
					    		if(RequeteEnvoyee.contains("/")) {
					    			RequeteEnvoyee="|"+RequeteEnvoyee+"|";
					    		}
								
								// Envoi des donnees ou commandes vers le Serveur
								FluxSortieEthernet.write(RequeteEnvoyee.getBytes());	//# Ecriture des donnees stockees dans RequeteEnvoyee pour le Serveur sur le flux de sortie
								FluxSortieEthernet.flush();
								// Attente de l accuse de reception du serveur
								if(AttendreAccuseReception("OK") == false)
								{
									CleanSockets();
									ServeurUp = false;
								}
								else
								{	// Si le client a demande a se deconnecter alors on le relance
									if(RequeteEnvoyee.contains("Cmd : ClientDeConnecte")) 
									{
										CleanSockets();
										ServeurUp = false;
									}
								}
							} 
							catch (IOException e)
							{
								System.out.println("Impossible d envoyer des donnees vers le serveur !!!\n");
								CleanSockets();
								ServeurUp = false;
							}
						}
					} 
					catch (IOException e) 
					{
						System.out.println("Impossible de recevoir des donnees depuis le serveur !!!\n");
						CleanSockets();
						ServeurUp = false;
					}
				} 
				catch (IOException e) 
				{
					System.out.println("Impossible d envoyer des donnees vers le serveur !!!\n");
					CleanSockets();
					ServeurUp = false;
				}
			} 
			catch (IOException e)
			{
				System.out.println("Impossible d etablir une communication avec le serveur !!!\n");
				CleanSockets();
				ServeurUp = false;
			}
			System.out.println("Fermeture du Client !!!");
		}
	}
	//------------------------------------------------------------------------------------------------------
	
	//############################################################################################################//    
	// Methode pour receptionner les accuses de reception du Serveur											  // 
	//						 									                                                  //
	// Les donnees a recues sont stockees dans un tableau de bytes									              //	
	// 																				  //
	//############################################################################################################// 
	public static boolean AttendreAccuseReception(String TypeAccuseReception )
	{
		String strAccuseReception = null;
    	byte byteTabAccuseReception [] = new byte[1500];
    	boolean EtatAttenteAccuseReception = true;
    	int intNbreCarLusEthernet = 0;
    	
		System.out.print("Attente de l accuse de reception Serveur : " );
		do
		{
			try 
			{
				intNbreCarLusEthernet = FluxEntreeEthernet.read(byteTabAccuseReception);	//# Lecture des donnees provenant du serveur stockage dans byteTabAccuseReception
				strAccuseReception=ConvTrameCar(byteTabAccuseReception,intNbreCarLusEthernet);
			} 
			catch (IOException e) 
			{
				System.out.println("Impossible de recevoir les accuses de reception depuis le serveur !!!\n");
				EtatAttenteAccuseReception = false;
			}
		}while(strAccuseReception!=null && !strAccuseReception.contains(TypeAccuseReception));
		
		if(EtatAttenteAccuseReception)
			System.out.println(" (" + TypeAccuseReception + ") Accuse de reception du serveur recu.");
		
		return EtatAttenteAccuseReception;
	}
	//------------------------------------------------------------------------------------------------------
	
	//########################################################################// 
	//		Méthode de fermeture des sockets TCP avec gestion des erreurs 	  //
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
			if(SocketClient!=null)
				SocketClient.close();
		} 
		catch (IOException e3) 
		{
			System.out.println("CleanSockets : Erreur sur Client.close() !!!");
		}		
		SocketClient=null;
	}
	//------------------------------------------------------------------------------------------------------
	
	
	//##############################################################################################//    
	// Methode pour afficher le contenu	de la trame	avec des caracters imprimables				    // 
	//							 									                               	//
	// les bytes sont convertis pour être affichés les caracteres non imprimables sont remplaces    //
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
}
