/**
 * -------------------------------------------------------------------
 * Nom du fichier : I2C_capteur.java
 * Auteur         : COSSON KILLIAN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Capteur I2C generique
 * -------------------------------------------------------------------
 * © 2025 COSSON KILLIAN - Tous droits reserves
 */

package capteurs;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CProvider;
import java.io.IOException;

public class I2C_Capteur extends CapteurGenerique {
    protected I2C device;
    protected int I2CAdresse;
    protected Context pi4j;
    protected I2CProvider i2cProvider;

    public I2C_Capteur(boolean VirtualDevice, int Adresse) throws IOException {
        super(); // Appelle le constructeur de CapteurGenerique
        this.VirtualDevice = VirtualDevice;
        this.Adresse = Adresse;
        this.pi4j = Pi4J.newAutoContext();

        this.i2cProvider = this.pi4j.provider("linuxfs-i2c");
    }

    public int getCptCapteurs() {
        return 1; // Par defaut, un capteur unique
    }

    public double[] getValues() {
        double[] values = new double[] { 0.0, 0.0, 0.0 }; // Initialise avec des valeurs a 0.0
        return (values != null) ? values : new double[] { -1.0 }; // Verification inutile ici
    }

    public int ReadReg(byte registre) throws IOException {
        // System.out.println("Lecture du registre : " + Integer.toHexString(registre &
        // 0xFF));
        return this.device.readRegister(registre & 0xFF);
    }

    public void WriteReg(byte registre, byte valeur) throws IOException {
        this.device.write(registre, valeur);
    }

    public void CloseI2CBus() {
        this.device.shutdown(pi4j);
    }
}
