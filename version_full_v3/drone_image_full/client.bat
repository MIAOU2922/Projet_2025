echo 0
javac -cp "lib/opencv-4100.jar" -d "bin" src/_start/*.java src/thread/*.java src/util/*.java src/main/*.java src/*.java
echo 1
java -Djava.library.path="lib" -cp "lib/opencv-4100.jar;bin" _start._start_client