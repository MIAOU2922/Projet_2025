import java.text.DecimalFormat;
import java.util.Scanner;
import java.lang.Math;
import com.forcedimension.sdk.*;
	
	
/**
 * ThreadJNIFalcon.java parts of Robot-Haptique-Console
 * 
 * @description: Thread de gestion de la boucle haptique du robot
 *
 * @author: Gruber Noe
 * @version: 1.1
 * @date: 01/04/2025
 * @modification: 01/04/2025
 */
public class ThreadJNIFalcon extends Thread
{
	private DHD monDHD = null; // Declaration de la propriete privee monDHD de type DHD (Device Haptic Driver) initialisee a null
	private DRD monDRD = null; // Declaration de la propriete privee monDRD de type DRD (Device Robot Driver) initialisee a null
	private Pilotage_manuel_par_pave_Souris monIHM;
	private int ValRetourForces = 1; // Declaration de la propriete privee ValRetourForces, initialisee a 1
	private double[] force = {0.0,0.0,0.0}; // Declaration de la propriete privee force de type tableau de double initialisee avec 3 doubles de valeur 0.0
	private double[] pos = {0.0,0.0,0.0}; // Declaration de la propriete privee pos de type tableau de double initialisee avec 3 doubles de valeur 0.0
	private boolean StopThread = false; // Declaration de la propriete privee StopThread de type boolean initialisee a faux
	private boolean haptiqueDisponible = false; // Pour signaler si le haptique a été détecté

	// Appel de la methode de classe GetSDKVersion pour obtenir les informations de version du driver Forcedimension
	static 
	{
		int sdk[] = new int[4]; // Declaration de la variable sdk de type tableau d entiers instanciee avec 4 entiers
        
		//DHD.GetSDKVersion(sdk); // Appeler la methode de classe GetSDKVersion de la classe DHD, passer a cette methode le tableau d entier sdk
	    System.out.println("----------------------------------------------------------------");
	    System.out.println("SDK Force Dimension - Java (JNI) version " + sdk[0] + "." + sdk[1] + "." + sdk[2] + "." + sdk[3]);
	    System.out.println("Copyright (C) 2001-2021 Force Dimension");
	    System.out.println("All Rights Reserved.");
	    
	}
		
	/**
	 * Constructeur de la classe par defaut
	 */
	public ThreadJNIFalcon(Pilotage_manuel_par_pave_Souris monIHMPrincipale)
	{
		monIHM = monIHMPrincipale;
	}
	
	/**
	 * Methode d'arret du Thread
	 */
	public void Close()
	{
		this.StopThread = true;  // Initialiser la propriete StopThread a vraie
	}
	
	/**
	 * Methode principale du Thread
	 */
	public void run() {
		while (!this.StopThread && !this.monIHM.getEtatConnexion()) {
			new Tempo(1);
		}

		Scanner ScanClavier = null;
		DecimalFormat df;
		double[] zerovect = {0.0, 0.0, 0.0};
		ScanClavier = new Scanner(System.in);
		df = new DecimalFormat("0.####");

		try {
			this.monDHD = new DHD();
			this.monDRD = new DRD();

			if (this.monDRD.Open() < 0) {
				System.err.println("Aucun périphérique détecté. Haptique non disponible.");
				return; // On quitte proprement le thread sans crash
			}

			if (!this.monDRD.IsSupported()) {
				System.err.println("Périphérique non supporté. Haptique non disponible.");
				this.monDRD.Close();
				this.monDHD.Close();
				return;
			}
			
			this.haptiqueDisponible = true; // Haptique bien disponible
			System.out.println("Robot Haptique NOVINT " + this.monDHD.GetSystemName() + " détecté.");
			this.monDHD.SetDeviceID(monDRD.GetDeviceID());

			System.out.println("Id: " + this.monDHD.GetDeviceID());
			System.out.println("Type: " + this.monDHD.GetSystemType());
			System.out.println("Nom: " + this.monDHD.GetSystemName());
			System.out.println("----------------------------------------------------------------");
			
			
			

			if (!((DRD) this.monDRD).IsInitialized() && this.monDHD.GetSystemType() == DHD.DEVICE_FALCON) {
				System.out.println("SVP initialisez le robot manuellement... Pousser le joystick à fond puis tirer le à fond.");
				while (!this.monDRD.IsInitialized()) {
					this.monDHD.SetForce(zerovect);
					new Tempo(1);
				}
			}

			if (!this.monDRD.IsRunning()) {
				if (!this.monDRD.IsInitialized() && this.monDRD.AutoInit() < 0) {
					System.err.println("Erreur d'initialisation du robot : " + DHD.GetLastErrorString());
					return;
				}
			}

			System.out.println("Robot initialisé...");
			this.monDHD.SetGravityCompensation(true);

			while (!this.StopThread) {
				this.monDHD.GetPosition(pos);

				this.pos[0] = (this.pos[0] * 3) + 0.05;
				this.pos[1] = (this.pos[1] * 2);
				this.pos[2] = (this.pos[2] * 4) - 0.1;

				this.monDHD.SetForce(this.force);
				this.monIHM.setVision(-this.pos[0], this.pos[1], this.pos[2]);

				new Tempo(1);
				
				//System.out.println("Button 0: "+this.monDHD.GetButton(0));

				if (monIHM.getEtatBoutonFermerServeur() || monIHM.getEtatBoutonQuitter()) {
					Close();
				}
			}
		} catch (Exception e) {
			System.err.println("Erreur lors de l'initialisation ou l'utilisation du robot haptique : " + e.getMessage());
			// Optionnel : afficher la pile complète pour le debug
			e.printStackTrace();
		} finally {
			if (ScanClavier != null) ScanClavier.close();

			if (this.monDHD != null) {
				this.monDHD.Close();
				new Tempo(200);
			}

			if (this.monDRD != null) {
				this.monDRD.Close();
				new Tempo(200);
			}
		}
	}
	
	public boolean isHaptiqueDisponible() {
		return this.haptiqueDisponible;
	}
	
	public int getDHDButton() {
		return this.monDHD.GetButton(0);
	}

	/**
	 * Methode de set pour le retour de force
	 */
	public void setRetourForce(int Value) 
	{
		this.ValRetourForces=Value;
	}
	
	
	
}
