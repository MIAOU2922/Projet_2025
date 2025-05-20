/**
 * -------------------------------------------------------------------
 * Nom du fichier : CustomClassLoader.java
 * Auteur         : COSSON KILLIAN
 * Version        : 3.0
 * Date           : 11/02/2025
 * Description    : Class pour charger dynamiquement les classes
 * -------------------------------------------------------------------
 * © 2025 COSSON KILLIAN - Tous droits reserves
 */

package util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class CustomClassLoader {
    public static ClassLoader createClassLoader(String libPath) throws Exception {
        File libDir = new File(libPath);
        File[] jarFiles = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            throw new IllegalArgumentException("No JAR files found in " + libPath);
        }

        URL[] urls = new URL[jarFiles.length + 1];
        for (int i = 0; i < jarFiles.length; i++) {
            urls[i] = jarFiles[i].toURI().toURL();
        }

        // Ajoutez le chemin des classes compilees
        urls[jarFiles.length] = new File("bin/").toURI().toURL();

        return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
    }
}
