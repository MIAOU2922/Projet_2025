/**
 * -------------------------------------------------------------------
 * Nom du fichier : I2C_GNSS.java
 * Auteur         : COSSON KILLIAN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Capteur GNSS I2C
 * -------------------------------------------------------------------
 * © 2025 COSSON KILLIAN - Tous droits reserves
 */


package capteurs;

// Import des bibliotheques necessaires
import java.util.Random;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;

public class I2C_GNSS extends I2C_Capteur {
    private static final int REG_LATITUDE = 0x00;
    private static final int REG_LONGITUDE = 0x04;
    private static final int REG_STATUS = 0x08;
    private static final int REG_SATELLITES = 0x0C;
    private static final int REG_ALTITUDE = 0x10; // Adresse fictive, remplacez par la bonne
    private static final int REG_SPEED = 0x14; // Adresse fictive, remplacez par la bonne
    private static final int REG_CONSTELLATION_CONFIG = 0x01; // Exemple : registre pour configurer les constellations
    private static final int CONSTELLATION_GALILEO = 0x04;   // Exemple : valeur pour activer Galileo

    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
    private int satellites;
    private boolean hasFix;
    private boolean flagStopThread, debugg;

    public I2C_GNSS(boolean _VirtualDevice, int _Adresse, String _Name, boolean _Debugg) throws Exception {
        super(_VirtualDevice, _Adresse);
        this.VirtualDevice = _VirtualDevice;
        this.setDeviceName(_Name);
        this.Adresse = _Adresse;
        this.flagStopThread = false;
        this.debugg = _Debugg;

        I2CConfig i2cConfig = I2C.newConfigBuilder(this.pi4j)
                .id(this.getDeviceName())
                .device(this.Adresse)
                .bus(1)
                .build();
        this.device = this.i2cProvider.create(i2cConfig);
        if (!VirtualDevice) {
            init();
        }
    }

    private boolean init() {
        try {
            System.out.println("Initialisation du GNSS en mode I2C...");
            int testRegister = this.device.readRegister(0x00);
            System.out.println("Test de lecture du registre 0x00 : " + testRegister);
            configureGalileo();
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du GNSS : " + e.getMessage());
            return false;
        }
    }

    public void run() {
        while (!flagStopThread) {
            try {
                if (!VirtualDevice) {
                    readData();
                } else {
                    generateVirtualData();
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Erreur dans le thread GNSS : " + e.getMessage());
            }
        }
    }

    public void Close() {
        this.flagStopThread = true;
        System.out.println("Arret du GNSS.");
    }

    private void readData() {
        try {
            hasFix = readStatus();
            satellites = readSatellites();
            if (!hasFix) {
                if(debugg)
                {
                    System.out.println("Pas de fix GPS. Satellites en vue : " + satellites);
                }
                return;
            }

            latitude = readCoordinate(REG_LATITUDE);
            longitude = readCoordinate(REG_LONGITUDE);
            altitude = readCoordinate(REG_ALTITUDE); // Lire l'altitude
            speed = readCoordinate(REG_SPEED);       // Lire la vitesse

            if (debugg) {
                System.out.printf("Latitude : %.6f, Longitude : %.6f, Altitude : %.2f, Vitesse : %.2f, Satellites : %d%n",
            latitude, longitude, altitude, speed, satellites);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture des donnees GNSS : " + e.getMessage());
        }
    }

    private boolean readStatus() {
        try {
            byte[] data = new byte[1];
            this.device.readRegister(REG_STATUS, data, 0, 1);
            return data[0] == 1;
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du statut GPS : " + e.getMessage());
            return false;
        }
    }

    private int readSatellites() {
        try {
            byte[] data = new byte[1];
            this.device.readRegister(REG_SATELLITES, data, 0, 1);
            return data[0] & 0xFF;
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du nombre de satellites : " + e.getMessage());
            return 0;
        }
    }

    private double readCoordinate(int register) {
        try {
            byte[] data = new byte[4];
            this.device.readRegister(register, data, 0, 4);
            return convertToFloat(data);
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture des coordonnees : " + e.getMessage());
            return 0.0;
        }
    }

    private double convertToFloat(byte[] data) {
        if (data == null || data.length != 4) {
            return 0.0;
        }

        int value = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);

        if (value > 0x7FFFFFFF) {
            value -= 0x100000000L;
        }

        return value / 10000000.0;
    }

    private void generateVirtualData() {
        this.latitude = rng(-90, 90);
        this.longitude = rng(-180, 180);
        this.satellites = (int) rng(0, 12);
        this.hasFix = satellites > 3;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getSatellites() {
        return satellites;
    }

    public double getAltitude() {
        return altitude;
    }
    
    public double getSpeed() {
        return speed;
    }

    public boolean hasFix() {
        return hasFix;
    }

    public static double rng(int min, int max) {
        return min + (max - min) * new java.util.Random().nextDouble();
    }

    private void configureGalileo() throws Exception {
        // ecrire dans le registre pour activer Galileo
        this.device.writeRegister(REG_CONSTELLATION_CONFIG, (byte) CONSTELLATION_GALILEO);
        System.out.println("Constellation Galileo activee.");
    }
}
