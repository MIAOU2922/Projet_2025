/**
 * -------------------------------------------------------------------
 * Nom du fichier : CapteurGenerique.java
 * Auteur         : COSSON KILLIAN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Class Capteur generique
 * -------------------------------------------------------------------
 * © 2025 COSSON KILLIAN - Tous droits reserves
 */


package capteurs;

import java.io.IOException;

public abstract class CapteurGenerique 
{
    protected int CptCapteurs;
    protected String Name;
    protected boolean Serveur;
    protected String ConnectionType;
    protected String DeviceType;
    protected String DeviceModel;
    protected boolean VirtualDevice;
    protected int Adresse;

    public CapteurGenerique() 
    {
    }
    public abstract int getCptCapteurs();

    public abstract double[] getValues();

    public abstract int ReadReg(byte registre) throws IOException;

    public abstract void WriteReg(byte registre, byte valeur) throws IOException;

    public abstract void CloseI2CBus();

    public String getConnectionType() 
    {
        return this.ConnectionType;
    }

    public String getDeviceType() 
    {
        return this.DeviceType;
    }

    public String getDeviceModel() 
    {
        return this.DeviceModel;
    }
    
    public String getDeviceName() 
    {
        return this.Name;
    }

    public void setDeviceName(String Name) 
    {
        this.Name = Name;
    }
}
