# Corona_Webapp
Admin Anmeldedaten: admin@gmail.com   Admin123


# Corona-Impfterminbuchung
Webanwendung zur Verwaltung und Buchung von Impfterminen.

## Funktionen
### Benutzerbereich

* Registrierung und Anmeldung
* Versand der Bestätigung per E-Mail
* Auswahl von Impfzentrum, Impfstoff und verfügbarem Zeitslot
* Buchung und Stornierung von Impfterminen
* Übersicht der eigenen Termine
* Terminbestätigung als PDF
* QR-Code zur Anzeige der Termininformationen
* Versand der Terminbestätigung per E-Mail

### Administrationsbereich

* Separate Admin-Anmeldung
* Verwaltung von Impfzentren
* Verwaltung von Impfstoffen
* Zuordnung von Impfstoffen zu Impfzentren
* Erstellung und Anzeige von 15-minütigen Zeitslots
* Zeitslots innerhalb der Öffnungszeit von 08:00 bis 18:00 Uhr

## Verwendete Technologien

* Java und Jakarta Servlets
* HTML, CSS und JavaScript
* MariaDB
* Apache Tomcat
* Jakarta Mail für den E-Mail-Versand
* iText für die PDF-Erstellung
* ZXing für die QR-Code-Erstellung
* Bash-Skripte für Build und Deployment

## Projektstruktur

| Ordner    | Beschreibung                                          |
| --------- | ----------------------------------------------------- |
| `app/`    | HTML-, CSS- und JavaScript-Dateien sowie `WEB-INF`    |
| `src/`    | Java-Quellcode, Servlets, Datenbank- und Hilfsklassen |
| `sql/`    | SQL-Datei zum Erstellen der Datenbanktabellen         |
| `bin/`    | Skripte für Konfiguration, Build und Deployment       |
| `lib/`    | Benötigte Java-Bibliotheken                           |
| `build/`  | Automatisch erzeugte Build-Dateien                    |
| `target/` | Erzeugte WAR-Datei                                    |

## Voraussetzungen

Für die Ausführung des Projekts werden folgende Programme benötigt:

* JDK 17 oder neuer
* MariaDB
* Apache Tomcat mit Jakarta-Unterstützung
* Bash
* `curl` für das Deployment

## Datenbank einrichten

Zuerst muss eine MariaDB-Datenbank erstellt werden. Danach können die Tabellen mit der vorhandenen SQL-Datei angelegt werden:

```bash
mariadb -u DEIN_DB_BENUTZER -p DEINE_DATENBANK < sql/CreateTable.sql
```

Die Datenbank-Zugangsdaten müssen lokal konfiguriert werden. Echte Benutzernamen und Passwörter dürfen nicht in GitHub gespeichert werden.

## Lokale Konfiguration

Die Deployment-Konfiguration wird durch folgendes Skript im Ordner `local/` erzeugt:

```bash
bash bin/configure.sh
```

Für den E-Mail-Versand wird die Datei `src/hbv/resources/config.properties` benötigt.

Beispiel:

```properties
EMAIL_SENDER=deine-email@gmail.com
EMAIL_PASSWORD=DEIN_GMAIL_APP_PASSWORT

mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.ssl.trust=smtp.gmail.com
```

## Projekt bauen und bereitstellen

Der komplette Build einschließlich Deployment wird mit folgendem Befehl ausgeführt:

```bash
bash bin/build.sh
```

Das Skript führt nacheinander folgende Schritte aus:

1. Alte Build-Dateien löschen
2. Anwendung vorbereiten
3. Java-Dateien kompilieren
4. WAR-Datei erzeugen
5. Anwendung über den Tomcat Manager bereitstellen

Die fertige Anwendung wird als folgende Datei erzeugt:

```text
target/webapp.war
```



