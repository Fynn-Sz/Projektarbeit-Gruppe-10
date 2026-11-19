-- Migration 002: Anrede (Frau/Herr) fuer Kunden bzw. deren Ansprechpartner.
-- Bisher ging die Anrede beim Import verloren ("Fr. Krause" -> nur "Krause").
--
-- Ausfuehren: einmalig NACH 001_kunde_altdaten.sql, als Schema-Besitzer (fynn).
--   Server: von Hand in DataGrip oder psql
--   CI:     automatisch in .github/workflows/test.yml (alle Dateien in db/migrationen/ der Nummer nach)
-- Das Skript darf gefahrlos mehrfach laufen (IF EXISTS / IF NOT EXISTS / WHERE ... IS NULL).

SET search_path TO maschinenverleih;

-- Gespeichert wird der Name der Java-Konstante (Enum Anrede) als Text, nicht als Nummer:
-- So ist der Wert in DataGrip ohne Code lesbar, und eine geaenderte Reihenfolge im Enum
-- kann keine vorhandenen Datensaetze verfaelschen.
-- Optional (NULL = keine Anrede): Firma ohne Ansprechpartner, im Altsystem unbekannt
-- oder die Person moechte weder mit "Frau" noch mit "Herr" angesprochen werden.
ALTER TABLE kunde ADD COLUMN IF NOT EXISTS anrede VARCHAR(10);
ALTER TABLE kunde DROP CONSTRAINT IF EXISTS ck_kunde_anrede;
ALTER TABLE kunde ADD CONSTRAINT ck_kunde_anrede CHECK (anrede IN ('FRAU', 'HERR'));

-- Beispielkunde aus data.sql (Max Mustermann) einordnen
UPDATE kunde SET anrede = 'HERR'
 WHERE kundennummer = 'K-0001' AND email = 'max@mustermann-bau.de' AND anrede IS NULL;
