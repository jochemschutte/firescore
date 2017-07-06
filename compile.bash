find -name "*.java" > sources.txt
javac -d bin @sources.txt
rm sources.txt

cd bin

find -name "*.class" > bin.txt

echo Main-Class: exec.FireScore > manifest.txt
jar cvfm ../FireScore.jar manifest.txt @bin.txt

echo Main-Class: generic.gui.ShotInput > manifest.txt
jar cvfm ../Input.jar manifest.txt @bin.txt

rm manifest.txt
rm bin.txt

