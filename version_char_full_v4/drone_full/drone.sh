#!/bin/bash

# Donner les permissions nécessaires
sudo chmod 777 -R ./

# Chemins vers les bibliothèques
LIB_PATH="./lib"
VL53L0X_LIB_PATH="./libs-VL53L0X"
OPENCV_LIB_PATH="$(pwd)/lib"

echo 0

# Exporter les variables d'environnement pour les bibliothèques natives
export LD_LIBRARY_PATH=$LIB_PATH:$VL53L0X_LIB_PATH:$OPENCV_LIB_PATH:$LD_LIBRARY_PATH

# Compilation
echo 1
javac --release 17 -cp "$LIB_PATH/*:$VL53L0X_LIB_PATH/*" -d "bin" src/_start/*.java src/thread/*.java src/util/*.java src/main/*.java src/capteurs/*.java src/gpio/*.java

echo 2
# Exécution du programme Java
java -Djava.library.path="$LIB_PATH:$VL53L0X_LIB_PATH:$OPENCV_LIB_PATH" -cp "$LIB_PATH/*:$VL53L0X_LIB_PATH/*:bin/." _start._start_drone