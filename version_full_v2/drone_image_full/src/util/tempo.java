/**
 * -------------------------------------------------------------------
 * Nom du fichier : tempo.java
 * Auteur         : BEAL JULIEN
 * Version        : 2.0
 * Date           : 11/02/2025
 * Description    : Classe pour gérer les délais
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package util;

public class tempo {
    
    public tempo(int _delay) {
        try {
            Thread.sleep(_delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }
}
