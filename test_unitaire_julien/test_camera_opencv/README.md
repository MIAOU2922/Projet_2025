# Projet Java avec OpenCV

## Prérequis
- Java Development Kit (JDK)
- OpenCV

## Configuration
1. Téléchargez et installez [OpenCV](https://opencv.org/releases/).
2. Ajoutez le fichier `opencv-<version>-<platform>/build/java/opencv-<version>.jar` à votre projet.
3. Ajoutez le chemin vers les bibliothèques natives d'OpenCV à la variable d'environnement `PATH`.

## Exécution
Compilez et exécutez le fichier `Main.java` :

```sh
javac -cp .;path/to/opencv-<version>.jar Main.java
java -cp .;path/to/opencv-<version>.jar Main
```
