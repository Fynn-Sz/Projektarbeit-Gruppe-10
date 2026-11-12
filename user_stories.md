# User Stories – Maschinenverleih & Abrechnung (Projekt-Backlog)

Jeder Abschnitt entspricht einer User Story bzw. einem GitHub Issue. 
Beim Anlegen in GitHub: Überschrift (`##`) als Issue-Titel verwenden, den Rest als Issue-Beschreibung übernehmen, Labels wie angegeben setzen.

---

## 1. Maschinen nach Verfügbarkeit suchen und reservieren

**Als** Kunde
**möchte ich** verfügbare Maschinen nach Kategorie, Zeitraum und Standort suchen und direkt reservieren können,
**damit** ich die passende Maschine für meinen Bedarf buchen kann, ohne vorher Rücksprache mit dem Vermieter halten zu müssen.

**Akzeptanzkriterien**
- [ ] Suchmaske mit Filtern für Kategorie, Verfügbarkeitszeitraum und Standort
- [ ] Ergebnisliste zeigt ausschließlich Maschinen, die im gewählten Zeitraum tatsächlich frei sind
- [ ] Reservierung wird verbindlich gespeichert, Kunde erhält eine Bestätigung
- [ ] Oberfläche ist gemäß ISO 9241 barrierefrei bedienbar

**Lernfeld:** LF10a – Benutzerschnittstellen gestalten und entwickeln (Fachrichtung Anwendungsentwicklung)
**Bündelungsfach:** Entwicklung vernetzter Prozesse
**Aufwand (T-Shirt-Größe):** L (16 Std.)

**Labels:** `user-story`, `lernfeld-LF10a`, `fachrichtung-AE`, `size-L`

---

## 2. Maschinenstammdaten erfassen und verwalten

**Als** Verleih-Mitarbeiter
**möchte ich** Maschinenstammdaten (Typ, Baujahr, Betriebsstunden, Wartungsintervalle) anlegen, bearbeiten und aus dem bestehenden Excel-System übernehmen können,
**damit** alle Maschinendaten zentral, konsistent und als Grundlage für spätere Auswertungen wie die Wartungsplanung verfügbar sind.

**Akzeptanzkriterien**
- [ ] Formular zum Anlegen/Bearbeiten von Maschinendatensätzen mit definierten Pflichtfeldern
- [ ] Importschnittstelle übernimmt bestehende Excel-Daten fehlerfrei
- [ ] Validierung verhindert doppelte oder fehlerhafte Einträge
- [ ] Änderungen werden nachvollziehbar protokolliert (Audit-Trail)

**Lernfeld:** LF5 – Software zur Verwaltung von Daten anpassen
**Bündelungsfach:** Softwaretechnologie und Datenmanagement
**Aufwand (T-Shirt-Größe):** M (8 Std.)

**Labels:** `user-story`, `lernfeld-LF5`, `size-M`

---

## 3. Wartungsbedarf auf Basis von Nutzungsdaten vorhersagen (Predictive Maintenance)

**Als** Werkstattplaner
**möchte ich** dass das System auf Basis der erfassten Nutzungsdaten (Betriebsstunden, Einsatzhäufigkeit, Ausfallhistorie) automatisch einen Wartungsbedarf bzw. -termin je Maschine berechnet und anzeigt,
**damit** ich Wartungen rechtzeitig einplanen kann, bevor es zu Ausfällen kommt.

**Akzeptanzkriterien**
- [ ] System berechnet aus den Nutzungsdaten einen Wartungs-Score oder -termin pro Maschine
- [ ] Maschinen mit überschrittenem oder nahendem Wartungstermin werden in einer Übersicht hervorgehoben
- [ ] Die Berechnungslogik (Modell/Regelwerk) ist dokumentiert und nachvollziehbar
- [ ] Ergebnisse lassen sich exportieren bzw. an die Werkstattplanung übergeben

**Lernfeld:** LF10c – Werkzeuge des maschinellen Lernens einsetzen (Fachrichtung Daten- und Prozessanalyse)
**Bündelungsfach:** Entwicklung vernetzter Prozesse
**Aufwand (T-Shirt-Größe):** XL (mehr als 16 Std.)

**Labels:** `user-story`, `lernfeld-LF10c`, `fachrichtung-DP`, `size-XL`

---

## 4. Server nach IT-Grundschutz absichern

**Als** Teammitglied (Systemverantwortung)
**möchte ich** den bereitgestellten Strato-Server nach den Basisabsicherungs-Maßnahmen des IT-Grundschutz einrichten,
**damit** ab dem ersten Tag eine sichere Grundlage besteht, auf der alle weiteren Komponenten aufgebaut werden können.

**Akzeptanzkriterien**
- [ ] SSH-Zugang nur per Key, Passwort-Login und Root-Login deaktiviert
- [ ] Firewall lässt ausschließlich benötigte Ports zu
- [ ] Automatische Sicherheitsupdates sind konfiguriert
- [ ] Eine einfache Schutzbedarfsanalyse für den Server ist dokumentiert (Schutzziele: Vertraulichkeit, Integrität, Verfügbarkeit)
- [ ] Umgesetzte Maßnahmen sind einem passenden IT-Grundschutz-Baustein zugeordnet

**Lernfeld:** LF4 – Schutzbedarfsanalyse im eigenen Arbeitsbereich durchführen
**Bündelungsfach:** Softwaretechnologie und Datenmanagement
**Aufwand (T-Shirt-Größe):** M (8 Std.)

**Labels:** `user-story`, `lernfeld-LF4`, `size-M`

---

## 5. Infrastruktur automatisiert bereitstellen (Infrastructure-as-Code)

**Als** Entwicklerteam
**möchte ich** die Basisinfrastruktur (Container-Technologie und Reverse Proxy) per Infrastructure-as-Code/Configuration-as-Code automatisiert aufsetzen können,
**damit** die gesamte Umgebung jederzeit reproduzierbar zerstört und neu aufgebaut werden kann, statt alles manuell zu konfigurieren.

**Akzeptanzkriterien**
- [ ] Ein Skript/Playbook (z. B. Ansible, Docker Compose) richtet Container-Laufzeit und Reverse Proxy vollautomatisch ein
- [ ] Die gesamte Konfiguration liegt versioniert im Git-Repository
- [ ] Nach vollständigem Löschen der Umgebung stellt ein einzelner Befehl sie wieder her
- [ ] Der Reverse Proxy leitet eine Testanfrage per HTTPS mit gültigem Zertifikat an einen Platzhalterdienst weiter

**Lernfeld:** LF9 – Netzwerke und Dienste bereitstellen
**Bündelungsfach:** Entwicklung vernetzter Prozesse
**Aufwand (T-Shirt-Größe):** L (16 Std.)

**Labels:** `user-story`, `lernfeld-LF9`, `size-L`

---

## 6. Lauffähiges Client-Server-Grundgerüst mit Datenbankanbindung aufsetzen

**Als** Entwicklerteam
**möchte ich** ein minimales, lauffähiges Grundgerüst aus Client, Server und Datenbank (angebunden über eine Datenbank-API) aufsetzen,
**damit** wir ab sofort an einzelnen fachlichen Funktionen (z. B. Maschinen- oder Kundenverwaltung) weiterarbeiten können, ohne die Basisarchitektur jedes Mal neu zu bauen.

**Akzeptanzkriterien**
- [ ] Die objektorientierte Server-Anwendung ist über den Reverse Proxy erreichbar
- [ ] Die Datenbankanbindung erfolgt ausschließlich über eine Datenbank-API (kein Tool, das die DB implizit erzeugt)
- [ ] Ein einfacher Testendpunkt schreibt/liest einen Beispieldatensatz in/aus der Datenbank
- [ ] Der Client kann sich mit dem Server verbinden und den Testendpunkt erfolgreich aufrufen
- [ ] Die Grundarchitektur ist als UML-Diagramm (z. B. Deployment- oder Komponentendiagramm) dokumentiert

**Lernfeld:** LF5 – Software zur Verwaltung von Daten anpassen
**Bündelungsfach:** Softwaretechnologie und Datenmanagement
**Aufwand (T-Shirt-Größe):** L (16 Std.)

**Labels:** `user-story`, `lernfeld-LF5`, `size-L`

---

## 7. Kundenstammdaten aus dem Altsystem übernehmen

**Als** Verleih-Mitarbeiter
**möchte ich** die bestehenden Kundendaten aus dem alten Excel-System über eine programmierte Schnittstelle in die neue Anwendung übernehmen und dort pflegen können,
**damit** kein Kunde beim Umstieg verloren geht und alle Kundendaten zentral im neuen System verfügbar sind.

**Akzeptanzkriterien**
- [ ] Importschnittstelle liest die Excel-Kundendaten ein und ordnet sie den Feldern des neuen Datenmodells zu
- [ ] Fehlerhafte oder unvollständige Datensätze werden beim Import erkannt und protokolliert statt stillschweigend übernommen
- [ ] Kundendaten lassen sich im neuen System anschließend anlegen, ändern und suchen
- [ ] Import ist wiederholbar, ohne Duplikate zu erzeugen

**Lernfeld:** LF8 – Daten systemübergreifend bereitstellen
**Bündelungsfach:** Softwaretechnologie und Datenmanagement
**Aufwand (T-Shirt-Größe):** L (16 Std.)

**Labels:** `user-story`, `lernfeld-LF8`, `size-L`

---

## 8. Rollen- und Zugriffsrechte verwalten

**Als** Systemverantwortlicher
**möchte ich** Benutzerkonten mit unterschiedlichen Rollen (z. B. Kunde, Mitarbeiter, Admin) und passenden Zugriffsrechten anlegen können,
**damit** jede Nutzergruppe nur auf die für sie vorgesehenen Daten und Funktionen zugreifen kann und die Anforderungen an Zugriffskontrollen gemäß GoBD erfüllt werden.

**Akzeptanzkriterien**
- [ ] Anmeldung erfordert gültige Zugangsdaten, Passwörter werden sicher gespeichert (Hashing)
- [ ] Jede Rolle hat klar definierte Rechte (z. B. Kunde sieht nur eigene Buchungen, Mitarbeiter sieht alle)
- [ ] Unautorisierte Zugriffsversuche auf fremde Daten werden abgelehnt und protokolliert
- [ ] Rollenzuordnung ist im System änderbar, ohne Code anzupassen

**Lernfeld:** LF11a – Funktionalität in Anwendungen realisieren (Fachrichtung Anwendungsentwicklung)
**Bündelungsfach:** Softwaretechnologie und Datenmanagement
**Aufwand (T-Shirt-Größe):** L (16 Std.)

**Labels:** `user-story`, `lernfeld-LF11a`, `fachrichtung-AE`, `size-L`

---

## 9. Buchung zu einem verbindlichen Auftrag mit Audit-Trail machen

**Als** Kunde
**möchte ich** dass meine Reservierung nach Bestätigung zu einem verbindlichen Auftrag mit eindeutiger Auftragsnummer wird, dessen Entstehung nachvollziehbar dokumentiert ist,
**damit** ich einen verlässlichen Nachweis über meine Buchung habe und der Verleiher die Anforderungen an compliance-konforme Datenerfassung erfüllt.

**Akzeptanzkriterien**
- [ ] Aus einer bestätigten Reservierung wird automatisch ein Auftrag mit eindeutiger Nummer, Kunde, Maschine und Zeitraum erzeugt
- [ ] Jede Statusänderung des Auftrags (angelegt, geändert, storniert) wird unveränderbar im Audit-Log protokolliert (Zeitstempel, Nutzer, Aktion)
- [ ] Der Auftrag ist Grundlage für die spätere Rechnungsstellung und kann nicht rückwirkend manipuliert werden
- [ ] Kunde und Mitarbeiter können den aktuellen Auftragsstatus jederzeit einsehen

**Lernfeld:** LF12a – Kundenspezifische Anwendungsentwicklung durchführen (Fachrichtung Anwendungsentwicklung)
**Bündelungsfach:** Gestaltung von IT-Dienstleistungen
**Aufwand (T-Shirt-Größe):** XL (mehr als 16 Std.)

**Labels:** `user-story`, `lernfeld-LF12a`, `fachrichtung-AE`, `size-XL`

---

## 10. Datenbanksystem kriteriengeleitet auswählen

**Als** Systemadministrator
**möchte ich** anhand fachlicher und wirtschaftlicher Kriterien ein konkretes Datenbanksystem auswählen,
**damit** eine begründete, dokumentierte Entscheidung vorliegt, bevor die Datenbank produktiv aufgesetzt wird, und die Datenbank-API-Anforderung aus den Rahmenbedingungen eingehalten wird.

**Akzeptanzkriterien**
- [ ] Mindestens drei relevante Datenbanksysteme sind recherchiert und gegenübergestellt
- [ ] Bewertungskriterien sind vorab festgelegt (z. B. Lizenzkosten, Skalierbarkeit, Backup-Funktionen, Kompatibilität mit der Client-Technologie, Community/Support)
- [ ] Entscheidung liegt als dokumentierte Nutzwertanalyse mit Punktebewertung vor
- [ ] Ausgewähltes System ist ausschließlich über eine Datenbank-API ansprechbar (kein Tool, das die Datenbank implizit erzeugt)
- [ ] Entscheidung ist im Team abgestimmt und dokumentiert (z. B. als Architecture Decision Record)

**Lernfeld:** LF9 – Netzwerke und Dienste bereitstellen
**Bündelungsfach:** Entwicklung vernetzter Prozesse
**Aufwand (T-Shirt-Größe):** M (8 Std.)

**Labels:** `user-story`, `lernfeld-LF9`, `fachrichtung-SI`, `size-M`

---

## 11. Datenbank-Server produktiv bereitstellen

**Als** Systemadministrator
**möchte ich** das ausgewählte Datenbanksystem automatisiert auf dem Strato-Server einrichten,
**damit** die Anwendung eine stabile, gesicherte und reproduzierbare Datenbasis nutzen kann.

**Akzeptanzkriterien**
- [ ] Datenbank läuft containerisiert und ist per IaC-Skript reproduzierbar aufsetzbar
- [ ] Zugriff ist auf notwendige Netzwerkports/-quellen beschränkt
- [ ] Zugangsdaten sind sicher hinterlegt (kein Klartext-Passwort im Repository)
- [ ] Regelmäßige automatisierte Backups sind eingerichtet und ein Restore wurde erfolgreich getestet
- [ ] Die Anwendung aus dem Grundgerüst kann sich erfolgreich verbinden

**Lernfeld:** LF10b – Serverdienste bereitstellen und Administrationsaufgaben automatisieren (Fachrichtung Systemintegration)
**Bündelungsfach:** Entwicklung vernetzter Prozesse
**Aufwand (T-Shirt-Größe):** L (16 Std.)

**Labels:** `user-story`, `lernfeld-LF10b`, `fachrichtung-SI`, `size-L`

---

## 12. Mailserver-System kriteriengeleitet auswählen

**Als** Systemadministrator
**möchte ich** aus den vorgegebenen Optionen (Mailcow, docker-mailserver, stalwart) anhand festgelegter Kriterien ein Mailserver-System auswählen,
**damit** eine begründete Entscheidung als Grundlage für Buchungsbestätigungen und den späteren Rechnungsversand vorliegt.

**Akzeptanzkriterien**
- [ ] Alle drei vorgegebenen Mailserver-Lösungen sind hinsichtlich Wartungsaufwand, Ressourcenbedarf, Funktionsumfang (z. B. DKIM/SPF, Webmail, API) und Docker-Kompatibilität verglichen
- [ ] Bewertungskriterien und Gewichtung sind vor der Bewertung festgelegt
- [ ] Entscheidung liegt als dokumentierte Nutzwertanalyse vor
- [ ] Gewähltes System ist mit der bestehenden Container- und Reverse-Proxy-Architektur kompatibel
- [ ] Entscheidung ist im Team abgestimmt und dokumentiert

**Lernfeld:** LF9 – Netzwerke und Dienste bereitstellen
**Bündelungsfach:** Entwicklung vernetzter Prozesse
**Aufwand (T-Shirt-Größe):** M (8 Std.)

**Labels:** `user-story`, `lernfeld-LF9`, `fachrichtung-SI`, `size-M`

---

## 13. Mailserver produktiv bereitstellen

**Als** Systemadministrator
**möchte ich** das ausgewählte Mailserver-System (z. B. Mailcow, docker-mailserver oder stalwart) automatisiert auf dem Strato-Server einrichten,
**damit** die Anwendung Buchungsbestätigungen und spätere Rechnungen zuverlässig und sicher per E-Mail versenden kann.

**Akzeptanzkriterien**
- [ ] Mailserver läuft containerisiert und ist per IaC-Skript reproduzierbar aufsetzbar
- [ ] SPF-, DKIM- und DMARC-Einträge sind korrekt konfiguriert, Testmails landen nicht im Spam
- [ ] Zugangsdaten und Postfächer sind sicher angelegt (kein Klartext-Passwort im Repository)
- [ ] Die Anwendung kann über die Mailserver-Schnittstelle erfolgreich eine Test-E-Mail versenden
- [ ] Backups der Mailserver-Konfiguration und -Daten sind eingerichtet und ein Restore wurde getestet

**Lernfeld:** LF10b – Serverdienste bereitstellen und Administrationsaufgaben automatisieren (Fachrichtung Systemintegration)
**Bündelungsfach:** Entwicklung vernetzter Prozesse
**Aufwand (T-Shirt-Größe):** L (16 Std.)

**Labels:** `user-story`, `lernfeld-LF10b`, `fachrichtung-SI`, `size-L`

---

## 14. Betrieb und Sicherheit der vernetzten Systeme gewährleisten

**Als** Systemadministrator
**möchte ich** Datenbank- und Mailserver kontinuierlich überwachen, härten und mit Sicherheitsupdates versorgen,
**damit** ein stabiler, sicherer Dauerbetrieb der Backend-Systeme gewährleistet ist und Ausfälle oder Sicherheitsvorfälle frühzeitig erkannt werden.

**Akzeptanzkriterien**
- [ ] Monitoring erfasst Verfügbarkeit, Ressourcenauslastung und Fehlerzustände von DB- und Mailserver
- [ ] Bei kritischen Zuständen (z. B. Dienst nicht erreichbar, Speicher voll) wird automatisch eine Benachrichtigung ausgelöst
- [ ] Sicherheitsupdates werden regelmäßig geprüft und dokumentiert eingespielt
- [ ] Ein einfacher Incident-Response-Ablauf (Was tun bei Ausfall/Angriff?) ist dokumentiert
- [ ] Zugriffslogs beider Dienste werden revisionssicher aufbewahrt

**Lernfeld:** LF11b – Betrieb und Sicherheit vernetzter Systeme gewährleisten (Fachrichtung Systemintegration)
**Bündelungsfach:** Softwaretechnologie und Datenmanagement
**Aufwand (T-Shirt-Größe):** L (16 Std.)

**Labels:** `user-story`, `lernfeld-LF11b`, `fachrichtung-SI`, `size-L`

---

## 15. TLS-Verschlüsselung automatisiert verwalten

**Als** Systemadministrator
**möchte ich** dass der Reverse Proxy TLS-Zertifikate automatisiert bezieht und erneuert,
**damit** alle Verbindungen zu Webportal, Online-Shop und Mailserver durchgehend verschlüsselt sind, ohne dass Zertifikate manuell nachgepflegt werden müssen.

**Akzeptanzkriterien**
- [ ] Reverse Proxy bezieht Zertifikate automatisiert (z. B. via Let's Encrypt) für alle relevanten Subdomains
- [ ] Erneuerung erfolgt automatisch vor Ablauf, ohne Downtime der Dienste
- [ ] Unverschlüsselte Verbindungen (HTTP) werden automatisch auf HTTPS umgeleitet
- [ ] Zertifikatskonfiguration ist Bestandteil des IaC-Setups und versioniert im Repository
- [ ] Ein Test mit einem SSL-Prüfwerkzeug bestätigt eine sichere Konfiguration (kein veraltetes TLS/keine schwachen Ciphers)

**Lernfeld:** LF9 – Netzwerke und Dienste bereitstellen
**Bündelungsfach:** Entwicklung vernetzter Prozesse
**Aufwand (T-Shirt-Größe):** M (8 Std.)

**Labels:** `user-story`, `lernfeld-LF9`, `fachrichtung-SI`, `size-M`

---
