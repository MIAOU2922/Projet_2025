echo 0
javac -cp "lib/opencv-4100.jar lib/forcedimension.jar" -d "bin" src/clavier/*.java src/thread/*.java src/util/*.java src/*.java
echo 1
java -Djava.library.path="lib" -cp "lib/opencv-4100.jar;bin" Pilotage_manuel_par_pave_Souris