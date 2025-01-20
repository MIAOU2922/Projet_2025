import org.opencv.core.*; // Import des classes principales de OpenCV
import org.opencv.videoio.VideoCapture; // Pour capturer des flux vidéo
import org.opencv.imgproc.Imgproc; // Pour le traitement d'images
import org.opencv.highgui.HighGui; // Pour afficher les fenêtres d'images
import org.opencv.imgcodecs.Imgcodecs; // Pour lire et écrire des fichiers image

import java.io.*; // Pour la gestion des fichiers
import java.net.*; // Pour les communications réseau
import java.util.*; // Pour les collections et les utilitaires
import java.util.Scanner; // Pour les entrées utilisateur

public class ImageSender {
    // Chargement de la bibliothèque native OpenCV
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("OpenCV library loaded successfully.");
    }

    // Variables pour les codes couleurs de la console
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

    // Variables globales pour la gestion de la vidéo et des images
    private static int boxWidth = 1280; // Largeur de l'image
    private static int boxHeight = 720; // Hauteur de l'image
    private static int offsetX = 0; // Décalage horizontal pour le traitement
    private static int frameCount = 0; // Compteur de trames
    private static long startTime = System.currentTimeMillis(); // Heure de début
    private static String imgDirPath = "img"; // Dossier pour sauvegarder les images
    private static String filePath = imgDirPath + "/frame_quality70.jpg"; // Chemin de l'image à sauvegarder

    private static String address; // Adresse IP du destinataire
    private static int port_image; // Port pour l'envoi des images
    private static int port_info; // Port pour l'envoi des informations

    private static double frequency, duration; // Fréquence (FPS) et durée du traitement
    private static long currentTime, sendTime; // Temps actuel et temps d'envoi
    private static String formattedFrequency, infoTextFPS, infoTextFrameCount, infoText; // Chaînes d'informations

    private static Queue<Long> frameTimes = new LinkedList<>(); // File pour stocker les temps des trames
    private static final int MAX_FRAMES = 100; // Nombre maximum de trames à mémoriser

    public static void main(String[] args) {
        // Scanner pour les entrées utilisateur
        Scanner scanner = new Scanner(System.in);

        // Demande à l'utilisateur si le mode broadcast doit être utilisé
        System.out.print("Utiliser le mode broadcast ? (oui/non) : ");
        String broadcastMode = scanner.nextLine().trim().toLowerCase();
        boolean isBroadcast = broadcastMode.equals("oui");

        // Demande à l'utilisateur d'entrer le port de référence
        System.out.print("Entrez le port de référence : ");
        int referencePort = scanner.nextInt();
        scanner.nextLine(); // Consomme la nouvelle ligne

        // Initialisation des ports pour les images et les informations
        port_image = referencePort;
        port_info = referencePort + 1;

        // Configuration de l'adresse IP (broadcast ou spécifique)
        if (isBroadcast) {
            address = "172.29.255.255"; // Adresse broadcast par défaut (à adapter)
        } else {
            System.out.print("Entrez l'adresse IP du récepteur : ");
            address = scanner.nextLine().trim();
        }

        // Création du dossier "img" s'il n'existe pas
        File imgDir = new File(imgDirPath);
        if (!imgDir.exists()) {
            imgDir.mkdir();
        }

        // Initialisation de la capture vidéo (caméra par défaut)
        VideoCapture capture = new VideoCapture(0);
        System.out.println("Tentative d'ouverture de la caméra vidéo...");
        if (!capture.isOpened()) {
            System.out.println(ANSI_RED + "Erreur : impossible d'ouvrir la caméra vidéo" + ANSI_RESET);
            return;
        }
        System.out.println(ANSI_GREEN + "Caméra vidéo ouverte avec succès." + ANSI_RESET);

        // Création des matrices pour stocker les images
        Mat frame = new Mat(); // Trame capturée
        Mat displayFrame = new Mat(); // Trame affichée

        // Création d'une fenêtre d'affichage
        HighGui.namedWindow("Sender", HighGui.WINDOW_NORMAL);
        HighGui.resizeWindow("Sender", boxWidth / 2, boxHeight / 2);

        // Boucle principale pour capturer et traiter les trames vidéo
        while (true) {
            // Capture d'une trame vidéo
            capture.read(frame);
            if (frame.empty()) {
                System.out.println(ANSI_RED + "Erreur : impossible de capturer une trame vidéo" + ANSI_RESET);
                break;
            }

            // Redimensionnement de l'image au format 16:9
            Imgproc.resize(frame, frame, new Size(boxWidth, boxHeight));
            displayFrame = Mat.zeros(boxHeight, boxWidth + offsetX, frame.type());
            frame.copyTo(displayFrame.colRange(offsetX, offsetX + boxWidth));

            // Création d'une version à moitié de la taille pour l'affichage
            Mat displayFrameHalfSize = new Mat();
            Imgproc.resize(displayFrame, displayFrameHalfSize, new Size(boxWidth / 2, boxHeight / 2));

            // Calcul des FPS (fréquence d'image)
            frameCount++;
            currentTime = System.currentTimeMillis();
            duration = (currentTime - startTime) / 1000.0;
            frequency = frameCount / duration;
            formattedFrequency = String.format("%.2f", frequency);

            // Affichage des informations (compteur et FPS) sur l'image
            infoTextFrameCount = "Nombre de trames : " + frameCount;
            infoTextFPS = "FPS : " + formattedFrequency;
            Imgproc.putText(displayFrameHalfSize, infoTextFrameCount, new Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
            Imgproc.putText(displayFrameHalfSize, infoTextFPS, new Point(10, 50), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);

            // Affichage de la trame dans la fenêtre
            HighGui.imshow("Sender", displayFrameHalfSize);

            // Sauvegarde de l'image redimensionnée
            Mat resizedFrame = new Mat();
            Imgproc.resize(displayFrame, resizedFrame, new Size(boxWidth, boxHeight));
            Imgcodecs.imwrite(filePath, resizedFrame, new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 70));

            // Envoi de l'image via UDP
            try {
                sendImageUDP(filePath, address, port_image);
                System.out.println(ANSI_YELLOW + "Image envoyée via UDP" + ANSI_RESET);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Envoi des informations (trames, FPS) via UDP
            sendTime = System.currentTimeMillis();
            infoText = infoTextFrameCount + "; " + infoTextFPS + "; " + "Temps d'envoi : " + sendTime;
            try {
                sendInfoUDP(infoText, address, port_info);
                System.out.println(ANSI_BLUE + "Informations envoyées via UDP" + ANSI_RESET);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Pause de 20 ms pour rafraîchir l'affichage (ESC pour quitter)
            if (HighGui.waitKey(1) == 27) {
                break;
            }
        }

        // Libération des ressources
        capture.release();
        HighGui.destroyAllWindows();
    }

    // Méthode pour envoyer une image via UDP
    private static void sendImageUDP(String filePath, String address, int port) throws IOException {
        File imgFile = new File(filePath);
        byte[] imgData = new byte[(int) imgFile.length()];
        try (FileInputStream fis = new FileInputStream(imgFile)) {
            fis.read(imgData);
        }
        DatagramSocket socket = new DatagramSocket();
        InetAddress ipAddress = InetAddress.getByName(address);
        DatagramPacket packet = new DatagramPacket(imgData, imgData.length, ipAddress, port);
        socket.send(packet);
        socket.close();
    }

    // Méthode pour envoyer des informations (texte) via UDP
    private static void sendInfoUDP(String info, String address, int port) throws IOException {
        byte[] infoData = info.getBytes();
        DatagramSocket socket = new DatagramSocket();
        InetAddress ipAddress = InetAddress.getByName(address);
        DatagramPacket packet = new DatagramPacket(infoData, infoData.length, ipAddress, port);
        socket.send(packet);
        socket.close();
    }
}
