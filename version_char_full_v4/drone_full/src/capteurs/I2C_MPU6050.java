/**
 * -------------------------------------------------------------------
 * Nom du fichier : I2C_MPU6050.java
 * Auteur         : Cosson Killian
 * Version        : 2.0
 * Date           : 25/03/2025
 * Description    : capteur pression, temperature et altitude
 * -------------------------------------------------------------------
 * © 2025 Cosson Killian - Tous droits reserves
 */
package capteurs;

// Import des bibliotheques necessaires
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;

//config capteur SEN_MPU6050
//VCC	3.3V (Pin 1)
//GND	GND (Pin 9)
//SDA	SDA (Pin 3)
//SCL	SCL (Pin 5)

public class I2C_MPU6050 extends I2C_Capteur {
    // Variables principales
    private double[] Values; // Stocke Acceleration X, Y, Z, Temperature, et Gyroscope X, Y, Z
    private double Agx, Agy, Agz;
    private double Gyrox, Gyroy, Gyroz;
    private boolean flagStopThread, debugg;
    private DecimalFormat df;

    // Registres specifiques au MPU6050
    private static final byte MPU6050_PWR_MGMT_1 = 0x6B;
    private static final byte MPU6050_ACCEL_XOUT_H = 0x3B;

    // Constructeur
    public I2C_MPU6050(boolean _VirtualDevice, int _Adresse, String _Name, boolean _Debugg) throws IOException {
        super(_VirtualDevice, _Adresse);
        VirtualDevice = _VirtualDevice;
        this.Name = _Name;
        this.Adresse = _Adresse;
        this.setDeviceName(this.Name);
        //=================================================================
        //mettre debugg = true si l'on veut recevoir les valeurs brutes 
        //
        this.debugg = _Debugg;
            I2CConfig i2cConfig = I2C.newConfigBuilder(this.pi4j)
                    .id(this.getDeviceName())
                    .device(this.Adresse) // Utilisation de l'adresse definie dans l'objet
                    .bus(1)
                    .build();
            this.device = this.i2cProvider.create(i2cConfig);
        this.Values = new double[7]; // Acceleration X, Y, Z, Temperature, et Gyroscope X, Y, Z
        this.flagStopThread = false;
        this.df = new DecimalFormat("#.##");

        if (!VirtualDevice) {
            init();
        }
    }

    // Methodes principales
    private void init() throws IOException {
        try {
            // Desactiver le mode veille
            WriteReg(MPU6050_PWR_MGMT_1, (byte) 0x00);
            System.out.println("MPU6050 initialise avec succes !");
            
            // Verifiez le registre WHO_AM_I
            int whoAmI = ReadReg((byte) 0x75);
            System.out.println("Registre WHO_AM_I : " + Integer.toHexString(whoAmI));
            if (whoAmI != 0x68 && whoAmI != 0x69) {
                throw new IOException("Erreur : WHO_AM_I invalide. Valeur lue : " + Integer.toHexString(whoAmI));
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'initialisation du MPU6050 : " + e.getMessage());
            throw e;
        }
    }

    public void run() {
        while (!flagStopThread) {
            try {
                if (!VirtualDevice) {
                    this.Values = readSensorData();
                } else {
                    this.Values = generateVirtualData();
                }
                if(debugg==true)
                {
                    System.out.println("Acceleration X: " + df.format(Values[0]) + " g");
                    System.out.println("Acceleration Y: " + df.format(Values[1]) + " g");
                    System.out.println("Acceleration Z: " + df.format(Values[2]) + " g");
                    System.out.println("Gyroscope X: " + df.format(Values[4]) + " °/s");
                    System.out.println("Gyroscope Y: " + df.format(Values[5]) + " °/s");
                    System.out.println("Gyroscope Z: " + df.format(Values[6]) + " °/s");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void Close() {
        this.flagStopThread = true;
        System.out.println("Fermeture du capteur MPU6050");
    }

    private double[] readSensorData() throws Exception {
        double[] data = new double[7]; // Inclut Acceleration X, Y, Z, Temperature, et Gyroscope X, Y, Z
        try {
            byte[] buffer = new byte[14];
            this.device.readRegister(MPU6050_ACCEL_XOUT_H, buffer, 0, 14);
            if(debugg==true)
            System.out.println("Donnees brutes MPU6050 : " + java.util.Arrays.toString(buffer));

            // Convertir les donnees brutes
            data[0] = convertToG(buffer[0], buffer[1]); // Acceleration X
            data[1] = convertToG(buffer[2], buffer[3]); // Acceleration Y
            data[2] = convertToG(buffer[4], buffer[5]); // Acceleration Z
            data[3] = convertToCelsius(buffer[6], buffer[7]); // Temperature
            data[4] = convertToDegreesPerSecond(buffer[8], buffer[9]); // Gyroscope X
            data[5] = convertToDegreesPerSecond(buffer[10], buffer[11]); // Gyroscope Y
            data[6] = convertToDegreesPerSecond(buffer[12], buffer[13]); // Gyroscope Z
            if(debugg==true)
            {
            System.out.println("Valeurs calculees MPU6050 : " + java.util.Arrays.toString(data));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture des donnees du MPU6050 : " + e.getMessage());
            throw e;
        }
        return data;
    }

    private double[] generateVirtualData() {
        Random random = new Random();
        return new double[] {
                rng(-2, 2), // Acceleration X
                rng(-2, 2), // Acceleration Y
                rng(-2, 2), // Acceleration Z
                rng(20, 40), // Temperature
                rng(-250, 250), // Gyroscope X
                rng(-250, 250), // Gyroscope Y
                rng(-250, 250) // Gyroscope Z
        };
    }

    // Methodes de conversion
    private double convertToG(byte msb, byte lsb) {
        int raw = ((msb & 0xFF) << 8) | (lsb & 0xFF);
        if (raw > 32767)
            raw -= 65536; // Convertir en entier signe
        return raw / 16384.0; // Conversion en g
    }

    private double convertToCelsius(byte msb, byte lsb) {
        int raw = ((msb & 0xFF) << 8) | (lsb & 0xFF);
        if (raw > 32767)
            raw -= 65536; // Convertir en entier signe
        return raw / 340.0 + 36.53; // Conversion en °C
    }

    private double convertToDegreesPerSecond(byte msb, byte lsb) {
        int raw = ((msb & 0xFF) << 8) | (lsb & 0xFF);
        if (raw > 32767)
            raw -= 65536; // Convertir en entier signe
        return raw / 131.0; // Conversion en degres par seconde
    }

    // Getters
    public double[] getValues() {
        return this.Values;
    }

    public double getAgx() {
        this.Agx = Values[0];
        return this.Agx;
    }

    public double getAgy() {
        this.Agy = Values[1];
        return this.Agy;
    }

    public double getAgz() {
        this.Agz = Values[2];
        return this.Agz;
    }

    public double getGyrox() {
        this.Gyrox = Values[4];
        return this.Gyrox;
    }

    public double getGyroy() {
        this.Gyroy = Values[5];
        return this.Gyroy;
    }

    public double getGyroz() {
        this.Gyroz = Values[6];
        return this.Gyroz;
    }

    // Methode utilitaire
    public static double rng(int min, int max) {
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }
}
