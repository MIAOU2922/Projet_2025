import java.net.*;

public class UDPSocketListener {
    public static void main(String[] args) {
        // Définition des ports UDP
    int port[] = {
        55000,
        55001,
        55002,
        55003,
        55004,
        55005,
        55006,
        55007,
        55008,
        55009
        };
        // Définition de la taille des paquets
        byte[] data = new byte[65536];
    
        // Initialisation des sockets UDP
        DatagramSocket[] sockets = new DatagramSocket[port.length];
        DatagramPacket[] packets = new DatagramPacket[port.length];

        // Création des sockets et des paquets
        for (int i = 0; i < port.length; i++) {
            try {
                sockets[i] = new DatagramSocket(port[i]);
                packets[i] = new DatagramPacket(data, data.length);

                // Lancement d'un thread pour chaque socket
                int finalI = i; // Nécessaire pour accéder à l'index dans le thread
                new Thread(() -> {
                    listenToSocket(sockets[finalI], packets[finalI]);
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Méthode pour écouter un socket
    private static void listenToSocket(DatagramSocket socket, DatagramPacket packet) {
        try {
            System.out.println("Écoute sur le socket : " + socket.getLocalPort());
            while (true) {
                socket.receive(packet);
                System.out.println("Données reçues sur le port " + socket.getLocalPort() + ": " + new String(packet.getData(), 0, packet.getLength()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
