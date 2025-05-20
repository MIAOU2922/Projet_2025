
/**
 * -------------------------------------------------------------------
 * Nom du fichier : DistanceTracker.java
 * Auteur         : GRUBER NOE
 * Modification   : BEAL JULIEN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Code de gestion de la distance parcourue par le drone
 * -------------------------------------------------------------------
 * © 2025 GRUBER NOE - Tous droits réservés
 */
package gpio;

import java.io.IOException;

import main.drone_telemetrie;

public class DistanceTracker extends Thread {
    private volatile boolean running = true;
    private final long LongueurCible;
    private final long LargeurCible;
    private final int NombreDePassage;
    private int NombrePassage;
    private double distanceParcourue = 0;
    private double distanceParcouruecontourner = 0;
    private boolean GD = true;
    private boolean commandeMoteur = false;
    
    private final drone_telemetrie drone;
    
    // Variables pour le calcul de l angle et de la vitesse
    private double angle = 0.0;
    private double vitesse = 0.0;
    private long dernierTempsAngle = 0;
    private long dernierTempsVitesse = 0;
    
    public DistanceTracker(long LongueurCible, long LargeurCible, int NombreDePassage, drone_telemetrie drone) throws Exception {
        this.LongueurCible = LongueurCible;
        this.LargeurCible = LargeurCible;
        this.NombreDePassage = NombreDePassage;
        this.drone = drone;
        
    }

    @Override
    public void run() {
        if (!running) return;

        NombrePassage = NombreDePassage;
        angle = 0.0;
        vitesse = 0.0;
        dernierTempsAngle = System.currentTimeMillis();
        dernierTempsVitesse = System.currentTimeMillis();
        while (NombrePassage > 1) {
            if (Serveur_Char_GPIO.StopModeAuto()) {
                System.out.println("Stop detecte ! Arret du robot.");
                Serveur_Char_GPIO.controleMoteur(0.0, 0.0);
                break;
            }

            parcoureLongueur();
            arreterMoteur();
            tournerRobot(GD);
            arreterMoteur();
            parcoureLargeur();
            arreterMoteur();
            tournerRobot(GD);
            arreterMoteur();

            GD = !GD;
            NombrePassage--;
        }

        parcoureLongueur();
        arreterMoteur();
        System.out.println("Mission terminee. Arret du robot.");
    }

    private void tournerRobot(boolean sens) {
        double angleRotation = sens ? 90.0 : -90.0;
        angle = 0.0;
        double facteurVitesse = 1.0;
        
        dernierTempsAngle = System.currentTimeMillis();

        //mpu6050.resetAngle();

        while (running && Math.abs(angle) < Math.abs(angleRotation)) {
            if (Serveur_Char_GPIO.StopModeAuto()) {
                System.out.println("Stop detecte ! Arret du robot.");
                arreterMoteur();
                break;
            }

            mettreAJourAngle();

            facteurVitesse = Math.max(0.5, 1.0 - Math.abs(angle / angleRotation));

            if (angleRotation > 0) {
                Serveur_Char_GPIO.controleMoteur(75.0 * facteurVitesse, 0.0);
            } else {
                Serveur_Char_GPIO.controleMoteur(-75.0 * facteurVitesse, 0.0);
            }

            attendre(50);
        }

        arreterMoteur();
        System.out.println("Rotation completee.");
    }

    private void parcoureLargeur() {
        distanceParcourue = 0;
        long lastTime = System.currentTimeMillis();

        while (running && distanceParcourue <= LargeurCible) {
            if (Serveur_Char_GPIO.StopModeAuto()) {
                System.out.println("Stop detecte ! Arret du robot.");
                break;
            }
            
            int distanceCapteur = drone.getDistance(); // val_0 = distance VL53L0X

            if (distanceCapteur < 150 && distanceCapteur != 0) {
                System.out.println("Obstacle detecte ! Manoeuvre d evitement. " + distanceCapteur);
                contournerObstacle();
            }

            if (!commandeMoteur) {
                Serveur_Char_GPIO.controleMoteur(0.0, 75.0);
                commandeMoteur = true;
            }

            distanceParcourue += calculerDistance(lastTime);
            lastTime = System.currentTimeMillis();
            attendre(50);
        }
    }

    private void parcoureLongueur() {
        distanceParcourue = 0;
        long lastTime = System.currentTimeMillis();

        while (running && distanceParcourue <= LongueurCible) {
            if (Serveur_Char_GPIO.StopModeAuto()) {
                System.out.println("Stop detecte ! Arret du robot.");
                break;
            }
            
            int distanceCapteur = drone.getDistance();

            if (distanceCapteur < 150 && distanceCapteur != 0) {
                System.out.println("Obstacle detecte ! Manoeuvre d evitement. " + distanceCapteur);
                contournerObstacle();
            }

            if (!commandeMoteur) {
                Serveur_Char_GPIO.controleMoteur(0.0, 75.0);
                commandeMoteur = true;
            }

            distanceParcourue += calculerDistance(lastTime);
            lastTime = System.currentTimeMillis();
            attendre(50);
        }
    }

    private double calculerDistance(long lastTime) {
    	mettreAJourVitesse();
        double deltaTime = (System.currentTimeMillis() - lastTime) / 1000.0;
        return vitesse * deltaTime;
    }
    
    private void mettreAJourVitesse() {
        long currentTime = System.currentTimeMillis();
        double accel = drone.getAgy(); // acceleration
        double deltaTime = (currentTime - dernierTempsVitesse) / 1000.0;
        vitesse += accel * deltaTime;
        dernierTempsVitesse = currentTime;
    }

    private void mettreAJourAngle() {
        long currentTime = System.currentTimeMillis();
        double gyroZ = drone.getGyroz(); // vitesse angulaire en deg/s
        double deltaTime = (currentTime - dernierTempsAngle) / 1000.0;
        angle += gyroZ * deltaTime;
        dernierTempsAngle = currentTime;
    }
    
    private void contournerObstacle() {
        System.out.println("Tentative de contournement de l obstacle...");
        arreterMoteur();
        tournerRobot(true);
        arreterMoteur();
        avancercontourner();
        arreterMoteur();
        tournerRobot(false);
        arreterMoteur();
        avancercontourner();
        arreterMoteur();
        distanceParcourue += 0.5;
        tournerRobot(false);
        arreterMoteur();
        avancercontourner();
        arreterMoteur();
        tournerRobot(true);
        arreterMoteur();
    }
    
    private void avancercontourner() {
    	distanceParcouruecontourner = 0;
        long lastTime = System.currentTimeMillis();

        while (running && distanceParcouruecontourner <= 0.5) {
            if (Serveur_Char_GPIO.StopModeAuto()) {
                System.out.println("Stop detecte ! Arret du robot.");
                break;
            }
            
            if (!commandeMoteur) {
                Serveur_Char_GPIO.controleMoteur(0.0, 75.0);
                commandeMoteur = true;
            }

            distanceParcouruecontourner += calculerDistance(lastTime);
            lastTime = System.currentTimeMillis();
            attendre(50);
        }
    }

    private void arreterMoteur() {
        Serveur_Char_GPIO.controleMoteur(0.0, 0.0);
        commandeMoteur = false;
    }

    private void attendre(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
