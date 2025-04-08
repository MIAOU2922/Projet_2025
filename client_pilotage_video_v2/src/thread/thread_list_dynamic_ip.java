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

public class thread_list_dynamic_ip extends Thread {

    private List<String> client_address = new ArrayList<>();
    private List<String> client_time = new ArrayList<>();
    private String name;
    private LocalDateTime clientTime , now;

    public thread_list_dynamic_ip(String name) {
        this.name = name;
        this.client_address = new ArrayList<>();
        this.client_time = new ArrayList<>();
        this.clientTime = LocalDateTime.now();
        this.now = LocalDateTime.now();
    }
    @Override
    public void run() {
        Thread.currentThread().setName(this.name);
        while (true) {
            try {
                this.now = LocalDateTime.now();
                for (int i = 0; i < this.client_time.size(); i++) {
                    this.clientTime = LocalDateTime.parse(this.client_time.get(i));
                    if (ChronoUnit.MINUTES.between(this.clientTime, now) > 3) {
                        System.out.println("\nAdresse " + this.client_address.get(i) + " supprimée pour inactivité.");
                        this.client_address.remove(i);
                        this.client_time.remove(i);
                        i--; // Ajuster l'index après la suppression
                    }
                }
                System.out.print("\033[F");  // Déplace le curseur à la ligne précédente
                System.out.print("\033[K");  // Efface la ligne
                System.out.println("Liste des adresses : " + client_address + " (" + client_address.size() + ") "+ client_time + " (" + client_time.size() + ")");
                Thread.sleep(10000); // Vérification toutes les 10 secondes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    // Ajouter un client à la liste
    public void addClient(String address ,String time) {
        if (client_address.indexOf(address) != -1) {
            updateClient(address);
            return;
        }else {
            client_address.add(address);
            client_time.add(time);
        }
    }
    // Supprimer un client de la liste
    public void removeClient(String address) {
        int index = client_address.indexOf(address);
        if (index != -1) {
            client_address.remove(index);
            client_time.remove(index);
        }
    }
    // Mettre à jour un client dans la liste
    public void updateClient(String address) {
        int index = client_address.indexOf(address);
        if (index != -1) {
            client_time.set(index, LocalDateTime.now().toString());
        }
    }
    // Obtenir la liste des adresses IP des clients
    public List<String> getClientAddress() {
        return new ArrayList<>(client_address);
    }
}
