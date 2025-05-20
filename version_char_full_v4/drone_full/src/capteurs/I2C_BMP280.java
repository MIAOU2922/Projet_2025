/**
 * -------------------------------------------------------------------
 * Nom du fichier : I2C_BMP280.java
 * Auteur         : COSSON KILLIAN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Capteur de temperature et pression BMP280
 * -------------------------------------------------------------------
 * © 2025 COSSON KILLIAN - Tous droits reserves
 */

//Config capteur BMP 280
// VCC = 1 (3.3V)
// GND = 9 (GND)
// SCL = 5 (BCM = 3)
// SDA = 3 (BCM = 2)
// CSB = 17 (3.3V)
// SDD = 14 (GND)

package capteurs;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

public class I2C_BMP280 extends I2C_Capteur {
    private String version;
    private boolean VirtualDevice;
    private int dig_T1;
    private int dig_T2;
    private int dig_T3;
    private double Temperature;
    private double Pression;
    private double Altitude;
    private int Bonne_Temperature;
    private int dig_P1, dig_P2, dig_P3, dig_P4, dig_P5, dig_P6, dig_P7, dig_P8, dig_P9;
    private DecimalFormat df;
    private boolean flagStopThread, debugg;
    private double[] Values;

    public I2C_BMP280(boolean _VirtualDevice, int _Adresse, String _Name, boolean _Debugg) throws IOException 
    {
        super(_VirtualDevice, _Adresse);
        //=================================================================
        //mettre debugg = true si l'on veut recevoir les valeurs brutes 
        //
        this.debugg = _Debugg;

        VirtualDevice = _VirtualDevice;
        this.Name = _Name;
        this.Adresse = _Adresse;
        if (VirtualDevice == false)
        {
            this.setDeviceName(this.Name);
            I2CConfig i2cConfig = I2C.newConfigBuilder(this.pi4j)
                    .id(this.getDeviceName())
                    .device(this.Adresse) // Utilisation de l'adresse definie dans l'objet
                    .bus(1)
                    .build();
            this.device = this.i2cProvider.create(i2cConfig);
            if (this.device != null) {
                System.out.println("   ");
                System.out.println("   ");
                System.out.println("BMP280 Device correctement initialise !");
                // Lire un registre pour tester
                try {
                    int valeur = this.device.readRegister(0xD0); // Registre ID du BMP280
                    System.out.println("ID du BMP280 : " + Integer.toHexString(valeur));
                } catch (Exception e) {
                    System.out.println("Erreur lors de la lecture : " + e.getMessage());
                }
            } else {
                System.out.println("ERREUR lors de l'initialisation de device");
                throw new IOException("Impossible d'initialiser device !");
            }
        }
        // Initialisation des autres variables
        this.version = "1.0";
        this.flagStopThread = false;
        this.Values = new double[3]; // Stocke Temperature, Pression, Altitude
        this.df = new DecimalFormat("#.##");
        if (!VirtualDevice)
        {
            init();
        }
    }

    private boolean init() {
        try {
            // Lire les coefficients de calibration du capteur BMP280
            byte[] calibData = new byte[24];
            this.device.readRegister(0x88, calibData, 0, 24); // Lire les 24 octets des coefficients de calibration
            // Coefficients de temperature
            this.dig_T1 = (calibData[1] << 8) | (calibData[0] & 0xFF);              // dig_T1 est un entier non signe 16 bits
            this.dig_T2 = (short) ((calibData[3] << 8) | (calibData[2] & 0xFF));    // dig_T2 est un entier signe 16 bits
            this.dig_T3 = (short) ((calibData[5] << 8) | (calibData[4] & 0xFF));    // dig_T2 est un entier signe 16 bits
            // Coefficients de pression
            this.dig_P1 = ((calibData[7] & 0xFF) << 8) | (calibData[6] & 0xFF);
            this.dig_P2 = (short) (((calibData[9] & 0xFF) << 8) | (calibData[8] & 0xFF));
            this.dig_P3 = (short) (((calibData[11] & 0xFF) << 8) | (calibData[10] & 0xFF));
            this.dig_P4 = (short) (((calibData[13] & 0xFF) << 8) | (calibData[12] & 0xFF));
            this.dig_P5 = (short) (((calibData[15] & 0xFF) << 8) | (calibData[14] & 0xFF));
            this.dig_P6 = (short) (((calibData[17] & 0xFF) << 8) | (calibData[16] & 0xFF));
            this.dig_P7 = (short) (((calibData[19] & 0xFF) << 8) | (calibData[18] & 0xFF));
            this.dig_P8 = (short) (((calibData[21] & 0xFF) << 8) | (calibData[20] & 0xFF));
            this.dig_P9 = (short) (((calibData[23] & 0xFF) << 8) | (calibData[22] & 0xFF));
            // Verification que les coefficients sont correctement extraits
            if(debugg==true)
            {
                System.out.println("dig_T1: " + this.dig_T1);
                System.out.println("dig_T2: " + this.dig_T2);
                System.out.println("dig_T3: " + this.dig_T3);
                System.out.println("dig_P1: " + this.dig_P1);
                System.out.println("dig_P2: " + this.dig_P2);
                System.out.println("dig_P3: " + this.dig_P3);
                System.out.println("dig_P4: " + this.dig_P4);
            }
            // Configuration du capteur
            WriteReg((byte) 0xF4, (byte) 0x27); // Configuration pour la temperature et la pression
            WriteReg((byte) 0xF5, (byte) 0xA0); // Configuration du temps de mesure
            return true;
        } catch (IOException e) {
            System.err.println("Erreur lors de l'initialisation des coefficients : " + e.getMessage());
            return false;
        }
    }

    public void run() {
        while (!flagStopThread) {
            try {
                this.Values[0] = this.CalculTemperature();
                this.Values[1] = this.CalculPression();
                this.Values[2] = this.CalculAltitude(1013.25);
                // Verification que les coefficients sont correctement extraits
                if(debugg==true)
                {
                    if (!VirtualDevice) 
                    {
                    System.out.println("dig_T1: " + this.dig_T1);
                    System.out.println("dig_T2: " + this.dig_T2);
                    System.out.println("dig_T3: " + this.dig_T3);
                    System.out.println("dig_P1: " + this.dig_P1);
                    System.out.println("dig_P2: " + this.dig_P2);
                    System.out.println("dig_P3: " + this.dig_P3);
                    System.out.println("dig_P4: " + this.dig_P4);
                    System.out.println("dig_T1: " + this.dig_T1);
                    System.out.println("dig_T2: " + this.dig_T2);
                    System.out.println("dig_T3: " + this.dig_T3);
                    }
                    
                    System.out.println("Temperature: " + df.format(Values[0]) + " C");
                    System.out.println("Pression: " + df.format(Values[1]) + " hPa");
                    System.out.println("Altitude: " + df.format(Values[2]) + " m");
                }
                
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void Close() {
        this.flagStopThread = true;
        System.out.println("Fermeture du serveur");
    }

    // ######################################################################################
    //
    // Getteur
    //
    // ######################################################################################
    public String getDeviceModel() {
        return "BMP280";
    }

    public double[] getValues() {
        return this.Values;
    }

    public double getTemperature() {
        this.Temperature = Values[0];
        return this.Temperature;
    }

    public double getPression() {
        this.Pression = this.Values[1];
        return this.Pression;
    }

    public double getAltitude() {
        this.CalculPression();
        this.Altitude = this.Values[2];
        return this.Altitude;
    }

    // Methode qui genere un double aleatoire entre -100 et 100
    public static double rng(int _min, int max) {
        Random random = new Random();
        return -_min + (max * 2 * random.nextDouble()); // Genere un nombre entre -100 et 100
    }

    // ######################################################################################
    //
    // Calcul
    //
    // ######################################################################################
    private double CalculTemperature() {
        if (!VirtualDevice) {
            byte[] tempData = new byte[3];
            this.device.readRegister(0xFA, tempData, 0, 3);

            if(debugg==true)
            System.out.println("Donnees brutes BMP280 (temperature) : " + java.util.Arrays.toString(tempData));

            int rawTemp = ((tempData[0] & 0xFF) << 12) | ((tempData[1] & 0xFF) << 4) | ((tempData[2] & 0xF0) >> 4);
            if(debugg==true)
            System.out.println("Temperature brute : " + rawTemp);

            int var1 = (((rawTemp >> 3) - (this.dig_T1 << 1)) * this.dig_T2) >> 11;
            int var2 = (((((rawTemp >> 4) - this.dig_T1) * ((rawTemp >> 4) - this.dig_T1)) >> 12) * this.dig_T3) >> 14;
            this.Bonne_Temperature = var1 + var2;

            if(debugg==true)
            System.out.println("Temperature calculee (intermediaire) : var1=" + var1 + ", var2=" + var2);

            return (this.Bonne_Temperature * 5 + 128) / 256.0 / 100.0; // Correction du calcul
        } else {
            return rng(-10, 50); // Valeur simulee pour le mode virtuel
        }
    }

    private double CalculPression() {
        if (this.VirtualDevice == false) {
            // Lire les donnees de pression (3 octets de 0xF7 a 0xF9)
            byte[] pressData = new byte[3];
            if (!VirtualDevice) {
                this.device.readRegister(0xF7, pressData, 0, 3); // Lecture des 3 octets de pression
            } else {
                pressData[0] = (byte) 0x00;
                pressData[1] = (byte) 0x00;
                pressData[2] = (byte) 0x00;
            }
            // Calcul de la pression brute (sur 20 bits)
            int rawPress = ((pressData[0] & 0xFF) << 12) | ((pressData[1] & 0xFF) << 4) | ((pressData[2] & 0xF0) >> 4);
            // Calcul de la pression en hPa (tout doit etre fait en double pour la
            // precision)
            double var1 = (this.Bonne_Temperature / 2.0) - 64000.0;
            double var2 = var1 * var1 * dig_P6 / 32768.0;
            var2 = var2 + var1 * this.dig_P5 * 2.0;
            var2 = (var2 / 4.0) + (this.dig_P4 * 65536.0);
            var1 = ((this.dig_P3 * var1 * var1 / 524288.0) + (this.dig_P2 * var1)) / 524288.0;
            var1 = (1.0 + var1 / 32768.0) * this.dig_P1;
            if (var1 == 0) {
                return 0; // Eviter la division par zero
            }
            double p = 1048576.0 - rawPress;
            p = (p - (var2 / 4096.0)) * 6250.0 / var1;
            var1 = this.dig_P9 * p * p / 2147483648.0;
            var2 = p * this.dig_P8 / 32768.0;
            p = p + (var1 + var2 + dig_P7) / 16.0;
            this.Values[1] = p / 100.0; // Pression en hPa
            if(debugg==true)
            System.out.println("Pression calculee: " + this.Values[1] + " hPa");
            return this.Values[1];
        } else {
            Values[1] = rng(0, 200);
            return this.Values[1]; // Convertir en Celsius
        }
    }

    private double CalculAltitude(double pression) throws IOException {
        if (this.VirtualDevice == false) {
            try {
                double pressure = this.CalculPression(); // Recuperer la pression actuelle
                if (pressure == -1) {
                    return -1; // Si la pression n'est pas valide, retourner NaN
                }
                // Calcul de l'altitude en fonction de la pression
                this.Values[2] = 44330.0 * (1.0 - Math.pow(pressure / pression, 0.1903));
                return this.Values[2];
            } catch (Exception e) {
                System.out.println("Erreur lors du calcul de l'altitude : " + e.getMessage());
                return -1;
            }
        } else {
            Values[2] = rng(0, 200);
            return this.Values[2]; // Convertir en Celsius
        }
    }

    public double CalculerAltitude(double _pression) {
        return this.Altitude = 44330.0 * (1.0 - Math.pow(this.Pression / _pression, 0.1903));
    }

    public double ConvertirCelsiusFarenheit(double val) {
        return (val * 9 / 5) + 32;
    }

    // ######################################################################################
    //
    // GPIO
    //
    // ######################################################################################
    // Methode Configuration d'une broche GPIO en entree avec resistance pull-down
    // Methode pour lire l'etat du GPIO
    public static int readGpio(DigitalInput pin) {
        return pin.isHigh() ? 1 : 0; // Retourne 1 si HIGH, sinon 0
    }
}
