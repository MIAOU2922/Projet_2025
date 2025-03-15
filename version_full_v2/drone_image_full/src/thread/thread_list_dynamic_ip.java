/**
 * -------------------------------------------------------------------
 * Nom du fichier : thread_list_dynamic_ip.java
 * Auteur         : BEAL JULIEN
 * Version        : 1.0
 * Date           : 14/03/2025
 * Description    : Thread d'envoie de commandes UDP
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package thread;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class thread_list_dynamic_ip {

    public static List<String> client_address = new ArrayList<>();
    public static List<String> client_time = new ArrayList<>();

    public static void main(String[] args) {
        // Thread pour vérifier les adresses toutes les minutes
        Thread.currentThread().setName("boucle d'afk");

        while (true) {
            try {
                LocalDateTime now = LocalDateTime.now();
                for (int i = 0; i < client_time.size(); i++) {
                    LocalDateTime clientTime = LocalDateTime.parse(client_time.get(i));
                    if (ChronoUnit.MINUTES.between(clientTime, now) > 3) {
                        System.out.println("Adresse " + client_address.get(i) + " supprimée pour inactivité.");
                        client_address.remove(i);
                        client_time.remove(i);
                        i--; // Ajuster l'index après la suppression
                    }
                }
                System.out.println("Liste des adresses : " + client_address + " (" + client_address.size() + ")" + client_time + " (" + client_time.size() + ")");
                Thread.sleep(10000); // Vérification toutes les 10 secondes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public static void addClient(String address) {
        client_address.add(address);
        client_time.add(LocalDateTime.now().toString());
    }

    public static void removeClient(String address) {
        int index = client_address.indexOf(address);
        if (index != -1) {
            client_address.remove(index);
            client_time.remove(index);
        }
    }

    public static void updateClient(String address) {
        int index = client_address.indexOf(address);
        if (index != -1) {
            client_time.set(index, LocalDateTime.now().toString());
        }
    }

    public static List<String> getClientAddress() {
        return client_address;
    }
}
