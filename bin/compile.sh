#!/usr/bin/env bash
source local/config.txt || exit 1

# Alle Java-Dateien finden
JAVAFILES=$(find src -name '*.java')
echo "COMPILE: $(echo $JAVAFILES|wc -w)"

# Java-Dateien kompilieren
javac -cp 'lib/*' -d build/WEB-INF/classes $JAVAFILES &&
        echo "COMPILE: success" || { echo "COMPILE: failure"; exit 1;}

# Schritt 1: Konfigurationsdatei nach build/WEB-INF/classes kopieren
echo "Kopiere config.properties nach build/WEB-INF/classes"
mkdir -p build/WEB-INF/classes/hbv/resources
cp src/hbv/resources/config.properties build/WEB-INF/classes/hbv/resources/

# Überprüfen, ob die Datei erfolgreich kopiert wurde
if [ -f build/WEB-INF/classes/hbv/resources/config.properties ]; then
#if [ -f build/WEB-INF/classes/config.properties ]; then 
echo "config.properties wurde erfolgreich kopiert!"
else
    echo "FEHLER: config.properties konnte nicht kopiert werden!"
    exit 1
fi

