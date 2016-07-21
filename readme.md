##JRaceTracker

This simple app reads from serial port and lists recorded laps in a GUI.

Currently supports the I-lap syntax (tested with a OpenLap reader).

###Building

- copy the directory maven/org into your .m2/repository directory
- install the native rxtx library from libs/rxtx-2.2pre2-bins.zip into your system jni library path
(e.g. /usr/lib/jni)
- run mvn package

###Running

java -Djava.library.path=/usr/lib/jni -cp lapreader-1.0-SNAPSHOT-jar-with-dependencies.jar com.kytomaki.jracetracker.App /dev/ttyUSB0 9600
sudo apt-get install librxtx-java

-Djava.library.path=/usr/lib/jni -Djava.library.path=/usr/lib/rxtx
java -Djava.library.path=/usr/lib/jni -cp lapreader-1.0-SNAPSHOT-jar-with-dependencies.jar com.kytomaki.jracetracker.LapReader
/usr/lib/jni/librxtxI2C.so