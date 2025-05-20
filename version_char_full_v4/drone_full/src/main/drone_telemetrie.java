/**
 * -------------------------------------------------------------------
 * Nom du fichier : drone_telemetrie.java
 * Auteur         : COSSON KILLIAN
 * Modification   : BEAL JULIEN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Code drone pour envoi d'image
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits reserves
 */

package main;

// Import des bibliotheques necessaires
import java.io.IOException;
import java.net.*;
import java.util.*;

import capteurs.*;
import thread.*;
import util.*;

public class drone_telemetrie {
    // Variables principales
    private int[] port = {55002, 55003 };
    private String address = "172.29.41.9";
    private String addressBroadcast = "172.29.255.255";
    private InetAddress addressLocal = null;
    private String addressLocalStr = "";

    private byte[] data = new byte[65536];
    private DatagramSocket socketCmd;
    private DatagramPacket packet;

    private String telemetrie_data = "";

    private long startTime;
    private int elapsedTime;

    private thread_reception_string commande;
    private thread_list_dynamic_ip listDynamicIp;

    private I2C_BMP280 bmp280;
    private I2C_MPU6050 mpu6050;
    private I2C_GNSS gnss;
    private I2C_VL53L0X vl53l0x; // Change Runnable to I2C_VL53L0X

    // Constructeur
    public drone_telemetrie(thread_reception_string commande, thread_list_dynamic_ip listDynamicIp) {
    
        this.commande = commande;
        this.listDynamicIp = listDynamicIp;

        // Initialisation des capteurs
        try {
            // verification de l'OS
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                this.bmp280 = new I2C_BMP280(true, 0x76, "BMP280", false);
                this.mpu6050 = new I2C_MPU6050(true, 0x69, "MPU6050",false);
                try {
                    this.gnss = new I2C_GNSS(true, 0x20, "GNSS",false); // Mode virtuel pour Windows
                } catch (Exception e) {
                    System.out.println("Erreur lors de l'initialisation du capteur GNSS en mode virtuel : " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                this.bmp280 = new I2C_BMP280(false, 0x76, "BMP280",false);
                this.mpu6050 = new I2C_MPU6050(false, 0x69, "MPU6050",false);
                try {
                    this.gnss = new I2C_GNSS(true, 0x20, "GNSS", false); // Mode reel pour Linux
                } catch (Exception e) {
                    System.out.println("Erreur lors de l'initialisation du capteur GNSS : " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // Initialisation du capteur VL53L0X avec une bibliotheque differente
            try {
                ClassLoader vl53ClassLoader = CustomClassLoader.createClassLoader("libs-VL53L0X");
                Class<?> vl53Class = Class.forName("capteurs.I2C_VL53L0X", true, vl53ClassLoader);
                this.vl53l0x = (I2C_VL53L0X) vl53Class
                        .getDeclaredConstructor(boolean.class, int.class, String.class, boolean.class)
                        .newInstance(false, 0x29, "VL53L0X", false);
            } catch (Exception e) {
                System.err.println("[ERROR] Erreur lors de l'initialisation du capteur VL53L0X : " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("[INFO] Tous les capteurs ont ete initialises avec succes.");
            // Lancement des threads de capteurs
            Thread sensorThread_bmp = new Thread(() -> {
                bmp280.run();
            });
            sensorThread_bmp.start();
            Thread sensorThread_vl53 = new Thread(() -> {
                if (vl53l0x != null) {
                    vl53l0x.run();
                }
            });
            sensorThread_vl53.start();
            Thread sensorThread_mpu6050 = new Thread(() -> {
                this.mpu6050.run();
            });
            sensorThread_mpu6050.start();
            Thread sensorThread_gnss = new Thread(() -> {
                gnss.run();
            });
            sensorThread_gnss.start();
        } catch (IOException e) {
            System.out.println("Erreur lors de l'initialisation des capteurs");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Erreur lors de l'initialisation des capteurs : " + e.getMessage());
            e.printStackTrace();
        }
        // Initialisation du timer
        this.startTime = System.currentTimeMillis();

        // Initialisation du compteur de temps
        this.elapsedTime = 0;

        // Boucle principale du drone
        try {
            error.printError();
            this.mainLoop();
        } catch (Exception e) {
            System.out.println("Erreur lors de l'execution de la boucle principale");
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            // Lancer les threads pour chaque capteur
            new Thread(() -> bmp280.run()).start();
            new Thread(() -> mpu6050.run()).start();
            new Thread(() -> gnss.run()).start();
            new Thread(() -> {
                try {
                    while (true) {
                        int distance = vl53l0x.getDistance();
                        System.out.println("Distance mesuree : " + distance + " mm");
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    System.err.println("[ERROR] VL53L0X: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            System.err.println("[ERROR] Erreur lors du demarrage des threads des capteurs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Methodes principales
    private void mainLoop() {
        long previousTime = System.currentTimeMillis(); // Temps initial

        while (true) {
            // Calculer le temps ecoule
            long currentTime = System.currentTimeMillis();
            if (currentTime - previousTime >= 1000) { // Verifier si 1 seconde s'est ecoulee
                elapsedTime++;
                previousTime += 1000; // Ajuster le temps de reference pour eviter les derives
            }
            StringBuilder telemetrieBuilder = new StringBuilder();
            if (vl53l0x != null)
            {
                telemetrieBuilder.append("dist:").append(vl53l0x.getDistance()).append(" ;");
            } else
            {
                telemetrieBuilder.append("dist:NA ;"); // NA pour indiquer que la distance n'est pas disponible
            }
            if (bmp280 != null)
            {
            telemetrieBuilder.append("temp:").append(bmp280.getTemperature()).append(" ;")
                            .append("alt:").append(bmp280.getAltitude()).append(" ;")
                            .append("baro:").append(bmp280.getPression()).append(" ;");
            } else
            {
                telemetrieBuilder.append("temp:NA ;")
                                .append("alt:NA ;")
                                .append("baro:NA ;");
            }
            if (mpu6050 != null)
            {
            telemetrieBuilder.append("agx:").append(this.mpu6050.getAgx()).append(" ;")
                            .append("agy:").append(this.mpu6050.getAgy()).append(" ;")
                            .append("agz:").append(this.mpu6050.getAgz()).append(" ;")
                            .append("gyrox:").append(mpu6050.getGyrox()).append(" ;")
                            .append("gyroy:").append(mpu6050.getGyroy()).append(" ;")
                            .append("gyroz:").append(mpu6050.getGyroz()).append(" ;");
            } else
            {
                telemetrieBuilder.append("agx:NA ;")
                                .append("agy:NA ;")
                                .append("agz:NA ;")
                                .append("gyrox:NA ;")
                                .append("gyroy:NA ;")
                                .append("gyroz:NA ;");
            }
            if (gnss != null)
            {
                telemetrieBuilder.append("lat:").append(gnss.getLatitude()).append(" ;")
                                .append("lon:").append(gnss.getLongitude()).append(" ;")
                                .append("gnss_alt:").append(gnss.getAltitude()).append(" ;")
                                .append("speed:").append(gnss.getSpeed()).append(" ;")
                                .append("satel:").append(gnss.getSatellites()).append(" ;");
            } else
            {
                telemetrieBuilder.append("lat:NA ;")
                                .append("lon:NA ;")
                                .append("gnss_alt:NA ;")
                                .append("speed:NA ;")
                                .append("satel:NA ;");
            }
            telemetrieBuilder.append("time:").append(elapsedTime).append("; ");

            telemetrie_data = telemetrieBuilder.toString();

            // Traitement des messages recus et envoi des donnees
            this.sendText();

            // Pause pour eviter une boucle trop rapide
            try {
                Thread.sleep(10); // Pause courte pour reduire la charge CPU
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sendText() {
        if (!this.listDynamicIp.getClientAddress().isEmpty()) {
            for (String addr : this.listDynamicIp.getClientAddress()) {
                try {
                    this.sendTextUDP(telemetrie_data, addr, this.port[1]);
                } catch (Exception e) {
                    System.out.println("Erreur lors de l'envoi du text: " + e.getMessage());
                }
            }
        }
    }

    private void sendTextUDP(String data, String address, int port) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress ipAddress = InetAddress.getByName(address);
            byte[] buffer = data.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipAddress, port);
            socket.send(packet);
            // System.out.println("Donnees envoyees a " + address + ":" + port);
            // System.out.println("Donnees envoyees : " + data);
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la donne a " + address + " : " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    // Getters pour les valeurs du BMP280
    public double getTemperature() {
        return bmp280 != null ? bmp280.getTemperature() : Double.NaN;
    }
    public double getAltitude() {
        return bmp280 != null ? bmp280.getAltitude() : Double.NaN;
    }
    public double getPression() {
        return bmp280 != null ? bmp280.getPression() : Double.NaN;
    }

    // Getters pour les valeurs du MPU6050
    public double getAgx() {
        return mpu6050 != null ? mpu6050.getAgx() : Double.NaN;
    }
    public double getAgy() {
        return mpu6050 != null ? mpu6050.getAgy() : Double.NaN;
    }
    public double getAgz() {
        return mpu6050 != null ? mpu6050.getAgz() : Double.NaN;
    }
    public double getGyrox() {
        return mpu6050 != null ? mpu6050.getGyrox() : Double.NaN;
    }
    public double getGyroy() {
        return mpu6050 != null ? mpu6050.getGyroy() : Double.NaN;
    }
    public double getGyroz() {
        return mpu6050 != null ? mpu6050.getGyroz() : Double.NaN;
    }

    // Getters pour les valeurs du GNSS
    public double getLatitude() {
        return gnss != null ? gnss.getLatitude() : Double.NaN;
    }
    public double getLongitude() {
        return gnss != null ? gnss.getLongitude() : Double.NaN;
    }
    public double getGnssAltitude() {
        return gnss != null ? gnss.getAltitude() : Double.NaN;
    }
    public double getGnssSpeed() {
        return gnss != null ? gnss.getSpeed() : Double.NaN;
    }
    public int getGnssSatellites() {
        return gnss != null ? gnss.getSatellites() : -1;
    }

    // Getter pour la distance VL53L0X
    public int getDistance() {
        return vl53l0x != null ? vl53l0x.getDistance() : -1;
    }
}
