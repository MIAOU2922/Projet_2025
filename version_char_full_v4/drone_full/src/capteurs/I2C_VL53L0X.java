/**
 * -------------------------------------------------------------------
 * Nom du fichier : I2C_VL53L0X.java
 * Auteur         : COSSON KILLIAN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Capteur de distance VL53L0X
 * -------------------------------------------------------------------
 * © 2025 COSSON KILLIAN - Tous droits reserves
 */

package capteurs;

// Import des bibliotheques necessaires
import java.io.IOException;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;

public class I2C_VL53L0X extends I2C_Capteur {
    // Variables principales
    private static final int VL53L0X_ADDRESS = 0x29; // Adresse I2C du capteur

    // Flag pour arreter le thread
    private boolean flagStopThread;
    private boolean Debugg; // Ajout du champ Debugg
    private static final int REG_START = 0x00;        // Registre de demarrage
    private static final int REG_RESULT = 0x1E;       // Registre où lire la distance
    private static final int REG_INTERRUPT_CLEAR = 0x0B; // Registre pour effacer les interruptions

    private I2C device;
    private int lastDistance = 5; // Derniere distance mesuree

    // Constructeur
    public I2C_VL53L0X(boolean _VirtualDevice, int _Adresse, String _Name, boolean _Debugg) throws Exception {
        super(_VirtualDevice, _Adresse);
        this.Name = _Name;
        this.VirtualDevice = _VirtualDevice;
        this.Debugg = _Debugg;

        if (!VirtualDevice) {
            try {
                Context pi4j = Pi4J.newAutoContext();
                I2CConfig config = I2C.newConfigBuilder(pi4j)
                        .id(_Name)
                        .bus(1)
                        .device(VL53L0X_ADDRESS)
                        .build();
                device = pi4j.create(config);
                System.out.println("[INFO] VL53L0X initialise !");
            } catch (Exception e) {
                System.err.println("[ERROR] VL53L0X: Exception during initialization - " + e.getMessage());
                throw new IOException("VL53L0X: Initialization failed!");
            }
        }
    }

    // Methode pour lire la distance
    public int calculDistance() {
        if (VirtualDevice) {
            return rng(0, 50); // Valeur simulee pour le mode virtuel
        }
        try {
            // Forcer une nouvelle mesure
            forceMeasurement();

            // Lecture de 2 octets pour la distance (poids fort et faible)
            byte[] buffer = new byte[2];
            device.readRegister(REG_RESULT, buffer, 0, 2); // Correction ici

            // Combinaison des bits poids fort et faible
            int distance = ((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF);

            // Effacer les interruptions pour permettre une nouvelle mesure
            device.writeRegister(REG_INTERRUPT_CLEAR, (byte) 0x01);

            // Filtrage logiciel : si distance < 30mm, on garde la dernière valeur valide
            if (distance < 30)
            {
                if (Debugg) {
                    System.out.println("[WARN] VL53L0X: Valeur trop basse (" + distance + " mm), on garde la dernière valeur (" + lastDistance + " mm)");
                }
                return lastDistance;
            }

            lastDistance = distance; // Mettre a jour la derniere distance mesuree
            return distance;
        } catch (Exception e) {
            System.err.println("[ERROR] VL53L0X: Erreur lors de la lecture de la distance - " + e.getMessage());
            return -1; // Valeur d'erreur
        }
    }

    // Methode pour recuperer la derniere distance mesuree
    public int getDistance() {
        return lastDistance;
    }

    // Methode pour forcer une nouvelle mesure
    private void forceMeasurement() throws Exception {
        // Demarrer une nouvelle mesure
        device.writeRegister(REG_START, (byte) 0x01);

        // Attendre que la mesure soit prete
        long startTime = System.currentTimeMillis();
        byte[] buffer = new byte[1]; // Correction ici
        while (true) {
            device.readRegister(REG_START, buffer, 0, 1); // Correction ici
            if ((buffer[0] & 0x01) == 0) {
                break;
            }
            if (System.currentTimeMillis() - startTime > 500) { // Timeout apres 500ms
                throw new Exception("Timeout : la mesure n'est pas prete.");
            }
        }
    }

    // Methode principale pour executer le capteur
    public void run() {
        while (!flagStopThread) {
            try {
                int distance = calculDistance();
                if(Debugg)
                {
                System.out.println("Distance mesuree : " + distance + " mm");
                }
                Thread.sleep(500); // Pause de 1 seconde entre chaque mesure
            } catch (Exception e) {
                System.err.println("[ERROR] VL53L0X: Erreur dans la boucle principale - " + e.getMessage());
            }
        }
    }

    // Methode pour fermer le capteur
    public boolean Close() {
        this.flagStopThread = true;
        System.out.println("Fermeture du capteur VL53L0X.");
        return true;
    }

    // Methode qui genere un nombre aleatoire (pour le mode virtuel)
    public static int rng(int _min, int _max) {
        return _min + (int) (Math.random() * ((_max - _min) + 1));
    }
}
