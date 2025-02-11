/**
 * -------------------------------------------------------------------
 * Nom du fichier : _start_all.java
 * Auteur         : BEAL JULIEN
 * Version        : 2.0
 * Date           : 11/02/2025
 * Description    : Lance tous les programmes
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package _start;

import main.client;
import main.drone;
import main.traitement;

public class _start_all {
    public static void main(String[] args) {
        // Création et démarrage de nouveaux threads pour chaque tâche
        Thread traitementThread = new Thread(() -> new traitement());
        Thread clientThread = new Thread(() -> new client());
        Thread droneThread = new Thread(() -> new drone());
        droneThread.start();
        traitementThread.start();
        clientThread.start();
    }
}

