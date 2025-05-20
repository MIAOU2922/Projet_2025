/**
 * -------------------------------------------------------------------
 * Nom du fichier : drone.java
 * Auteur         : BEAL JULIEN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Code drone pour envoi d'image
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package main;

import java.io.*;
import java.net.*;
import java.util.*;

import org.opencv.core.*;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import thread.*;
import util.*;

public class drone_pipeline {

    static {

        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (Exception e) {
            System.out.println("\nErreur lors du chargement des librairies: " + e);
        }
    }

    private final int PORT = 55000;
    private final String BROADCAST_ADDRESS = "172.29.255.255";
    private final int QUALITY = 50;
    private final Size TARGET_SIZE = new Size(1280, 720);
    private final MatOfInt JPEG_PARAMS = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, QUALITY);
    private final MatOfByte BUFFER = new MatOfByte();
    
    private VideoCapture capture;
    private Mat frame;
    private long previousTime;
    private DatagramSocket socket;
    private InetAddress broadcastAddress;
    private byte[] imageBuffer;
    private DatagramPacket packet;

    public drone_pipeline() {
        try {
            // Ajouter avant l'initialisation de la caméra:
            System.setProperty("LD_LIBRARY_PATH", "/usr/lib/gstreamer-1.0");
            Core.setNumThreads(4); // Optimiser le multithreading

            // Pré-allocation des ressources
            frame = new Mat();
            imageBuffer = new byte[1280 * 720 * 3]; // Taille maximale estimée
            
            // Configuration réseau
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);
            packet = new DatagramPacket(imageBuffer, 0, broadcastAddress, PORT);

            // Initialisation caméra
            try {
                // Direct capture for Logitech webcam
                capture = new VideoCapture(0);
                Thread.sleep(2000); // Wait for camera initialization
                
                // Basic settings first
                capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);  // Start with lower resolution
                capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
                capture.set(Videoio.CAP_PROP_FPS, 30);
                
                if (!capture.isOpened()) {
                    throw new RuntimeException("Impossible d'ouvrir la caméra (device 0)");
                }

                // Advanced settings after successful open
                // capture.set(Videoio.CAP_PROP_FOURCC, VideoWriter.fourcc('M','J','P','G'));
                // capture.set(Videoio.CAP_PROP_BUFFERSIZE, 3);
                
                // // Scale up to target resolution if initial connection successful
                // capture.set(Videoio.CAP_PROP_FRAME_WIDTH, TARGET_SIZE.width);
                // capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, TARGET_SIZE.height);

                // Verify final settings
                double actualWidth = capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
                double actualHeight = capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
                System.out.printf("Camera initialized: %.0fx%.0f@%.0ffps%n", 
                    actualWidth, actualHeight, 
                    capture.get(Videoio.CAP_PROP_FPS));
                    
            } catch (Exception e) {
                System.out.println("Erreur lors de l'initialisation de la caméra: " + e.getMessage());
                throw e;
            }

            // Configuration de la caméra pour les performances
            if (!capture.isOpened()) {
                // Try fallback to direct capture
                capture.release();
                capture = new VideoCapture(0);
                capture.set(Videoio.CAP_PROP_FRAME_WIDTH, TARGET_SIZE.width);
                capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, TARGET_SIZE.height);
                capture.set(Videoio.CAP_PROP_FPS, 30);
                
                if (!capture.isOpened()) {
                    throw new RuntimeException("Impossible d'ouvrir la caméra");
                }
            }

            capture.set(3, TARGET_SIZE.width);  // Width
            capture.set(4, TARGET_SIZE.height); // Height
            capture.set(5, 30);                 // Target FPS
            
            mainLoop();
        } catch (Exception e) {
            System.out.println("\nErreur d'initialisation");
            e.printStackTrace();
            cleanup();
        }
    }

    private void mainLoop() {
        previousTime = System.nanoTime();
        try {
            while (true) {
                if (!capture.read(frame)) break;

                // Compression et envoi
                Imgcodecs.imencode(".jpg", frame, BUFFER, JPEG_PARAMS);
                byte[] data = BUFFER.toArray();
                
                // Mise à jour du packet sans nouvelle allocation
                packet.setData(data, 0, data.length);
                socket.send(packet);

                // Calcul FPS optimisé
                long now = System.nanoTime();
                double fps = 1_000_000_000.0 / (now - previousTime);
                previousTime = now;
                System.out.printf("\rFPS: %.1f", fps);
            }
        } catch (Exception e) {
            System.out.println("\nErreur dans la boucle principale");
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        if (capture != null) capture.release();
        if (socket != null && !socket.isClosed()) socket.close();
        if (frame != null) frame.release();
        BUFFER.release();
        JPEG_PARAMS.release();
    }

    public static void main(String[] args) {
        new drone_pipeline();
    }
}
