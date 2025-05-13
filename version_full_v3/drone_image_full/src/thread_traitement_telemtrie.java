/*
*-------------------------------------------------------------------
* Nom du fichier : thread_traitement_telemtrie.java
* Auteur         : BEAL JULIEN
* Version        : 3.1
* Date           : 11/02/2025
* Description    : Classe traitement pour gérer la telemetrie
*-------------------------------------------------------------------
* © 2025 BEAL JULIEN - Tous droits réservés
*/



import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.time.LocalDateTime;
import javax.swing.ImageIcon;

import org.opencv.core.Core;

import thread.*;
import util.*;

public class thread_traitement_telemtrie extends Thread {
    static {
        try {
            System.loadLibrary("JNIFileMappingDroneCharTelemetryServeur");
        } catch (Exception e) {
            System.err.println("\nErreur lors du chargement de la librairie JNIFileMappingDroneCharTelemetryServeur");
            e.printStackTrace();
        }
        try {
            System.loadLibrary("JNIVirtualDroneCharTelemetry");
        } catch (Exception e) {
            System.err.println("\nErreur lors du chargement de la librairie JNIVirtualDroneCharTelemetry");
            e.printStackTrace();
        }
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (Exception e) {
            System.err.println("\nErreur lors du chargement de la librairie OpenCV");
            e.printStackTrace();
        }
    }

    // Déclarations des objets FileMapping

    private cFileMappingDroneCharTelemetryServeur serveur_filemap_telemetrie = new cFileMappingDroneCharTelemetryServeur(false);

    // Variables liées au traitement

    private String address_local_str, text;

    // Configuration réseau et UDP
    private byte[] data = new byte[65536];
    private DatagramSocket socketTelemetrie;
    private DatagramPacket packet;
    private InetAddress localAddress;


    // Threads
    private thread_reception_string telemetrie;

    // Variables
    private String telemetryRecu , name;
    private String[] parts;

    // --------------------------------------------------------------//
    public thread_traitement_telemtrie(String _name , DatagramSocket _socket) {

        this.name = _name ;
        this.socketTelemetrie = _socket ;
    }

    @Override
    public void run() {

        // --------------------------------------------------------------//
        // Ouverture du serveur de FileMapping pour la telemetrie
        try {
            this.serveur_filemap_telemetrie.OpenServer("telemetrie_java_to_c");
        } catch (Exception e) {
            System.out.println("\nErreur lors de l'ouverture du serveur telemetrie_java_to_c");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // init du filemap de telemetrie a 0
        for (int i = 0; i < 20; i++) {
            try {
                Method method = this.serveur_filemap_telemetrie.getClass()
                        .getMethod("set_val_" + i, double.class);
                method.invoke(this.serveur_filemap_telemetrie, (double) i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // --------------------------------------------------------------//
        // Lancement du thread
        try {
            //thread telemetrie
            this.telemetrie = new thread_reception_string(this.name, this.socketTelemetrie);
            this.telemetrie.start();
        } catch (Exception e) {
            System.out.println("\nErreur lors du lancement des threads");
            e.printStackTrace();
        }
        // --------------------------------------------------------------//
        // Boucle principale de traitement
        try {
            this.mainLoop();
        } catch (Exception e) {
            System.out.println("\nErreur lors de la boucle principale de traitement");
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------//
    // Boucle principale de traitement
    private void mainLoop() {
        while (true) {
            // Traitement du message UDP reçu
            this.parts = null;
            this.telemetryRecu = this.telemetrie.getMessageRecu();
            if (!this.telemetryRecu.isEmpty()) {
                this.parts = this.telemetryRecu.split(";");
                for (String part : this.parts) {
                    String[] keyValue = part.split(":");
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim();
                        String value = keyValue[1].trim();
                        //System.out.println(key + " : " + value);
                        try {
                            double valueToSet = value.equals("NA") ? 0.0 : Double.parseDouble(value);
                            
                            switch(key) {
                                case "dist":
                                    this.serveur_filemap_telemetrie.set_val_0(valueToSet);
                                    break;
                                case "temp":
                                    this.serveur_filemap_telemetrie.set_val_1(valueToSet);
                                    break;
                                case "alt":
                                    this.serveur_filemap_telemetrie.set_val_2(valueToSet);
                                    break;
                                case "baro":
                                    this.serveur_filemap_telemetrie.set_val_3(valueToSet);
                                    break;
                                case "agx":
                                    this.serveur_filemap_telemetrie.set_val_4(valueToSet);
                                    break;
                                case "agy":
                                    this.serveur_filemap_telemetrie.set_val_5(valueToSet);
                                    break;
                                case "agz":
                                    this.serveur_filemap_telemetrie.set_val_6(valueToSet);
                                    break;
                                case "gyrox":
                                    this.serveur_filemap_telemetrie.set_val_7(valueToSet);
                                    break;
                                case "gyroy":
                                    this.serveur_filemap_telemetrie.set_val_8(valueToSet);
                                    break;
                                case "gyroz":
                                    this.serveur_filemap_telemetrie.set_val_9(valueToSet);
                                    break;
                                case "lat":
                                    this.serveur_filemap_telemetrie.set_val_10(valueToSet);
                                    break;
                                case "lon":
                                    this.serveur_filemap_telemetrie.set_val_11(valueToSet);
                                    break;
                                case "gnss_alt":
                                    this.serveur_filemap_telemetrie.set_val_12(valueToSet);
                                    break;
                                case "speed":
                                    this.serveur_filemap_telemetrie.set_val_13(valueToSet);
                                    break;
                                case "satel":
                                    this.serveur_filemap_telemetrie.set_val_14(valueToSet);
                                    break;
                                case "time":
                                    this.serveur_filemap_telemetrie.set_val_15(valueToSet);
                                    break;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Erreur de conversion pour la clé " + key + " : " + value);
                        }
                    }
                }
            }
            // Réinitialisation
            this.telemetryRecu = "";
            this.parts = null;
            new tempo(1);
        }
    }
}
