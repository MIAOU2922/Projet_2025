/**
 * -------------------------------------------------------------------
 * Nom du fichier  : tempo.java
 * Auteur         : BEAL JULIEN
 * Version        : 1.0
 * Date           : 03/02/2025
 * Description    : class tempo
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
