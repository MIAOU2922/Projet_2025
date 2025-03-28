
sudo chmod 777 -R ./


#!/bin/bash

# Chemin vers les bibliothèques OpenCV
OPENCV_LIB_PATH="/home/Partage/drone_image_full/lib"

echo 0

# Exporter les variables d'environnement
export LD_LIBRARY_PATH=$OPENCV_LIB_PATH:$LD_LIBRARY_PATH
export JAVA_OPTS="-Djava.library.path=$OPENCV_LIB_PATH -cp lib/opencv-4100.jar:bin"


# Compilation
echo 1
javac -cp "lib/opencv-4100.jar" -d "bin" src/_start/*.java src/thread/*.java src/util/*.java src/main/*.java src/*.java

echo 2
# Exécuter le script Java
java $JAVA_OPTS _start._start_drone
