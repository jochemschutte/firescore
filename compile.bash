rm -rf bin/
mkdir bin
find -name "*.java" > sources.txt
javac -d bin @sources.txt
echo "_compiling to .class"
rm sources.txt

cd bin

find -name "*.class" > bin.txt

echo Main-Class: exec.FireScore > manifest.txt
jar cvfm ../FireScore.jar manifest.txt @bin.txt
echo "_compiling FireScore.jar"

echo Main-Class: generic.gui.ShotInput > manifest.txt
jar cvfm ../Input.jar manifest.txt @bin.txt
echo "_compiling Input.jar"

echo Main-Class: move.Move > manifest.txt
jar cvfm ../Move.jar manifest.txt @bin.txt
echo "_compiling Move.jar"

rm manifest.txt
rm bin.txt
cd ..
rm -rf bin
echo "_finished!"
