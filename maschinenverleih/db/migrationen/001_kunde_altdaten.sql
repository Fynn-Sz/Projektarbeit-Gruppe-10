-- Migration 001: Kundentabelle fuer die Uebernahme der Altdaten aus Excel vorbereiten
-- (Ticket "Kundendaten aus dem alten Excel-System uebernehmen").
--
-- Ausfuehren: einmalig NACH db/schema.sql, als Schema-Besitzer (fynn).
--   Server: von Hand in DataGrip oder psql
--   CI:     automatisch in .github/workflows/test.yml (alle Dateien in db/migrationen/ der Nummer nach)
-- Das Skript darf gefahrlos mehrfach laufen (IF EXISTS / IF NOT EXISTS / WHERE ... IS NULL).

SET search_path TO maschinenverleih;

-- 1. Pflichtfelder lockern, die das Altsystem nicht immer liefert:
--    Bei Firmen steht oft nur "Frau Krause" als Ansprechpartner -> Vorname optional.
--    Einige Kunden haben keine E-Mail-Adresse                    -> E-Mail optional.
--    Die E-Mail bleibt trotzdem eindeutig (UNIQUE erlaubt in PostgreSQL mehrere NULL-Werte).
ALTER TABLE kunde ALTER COLUMN vorname DROP NOT NULL;
ALTER TABLE kunde ALTER COLUMN email   DROP NOT NULL;

-- 2. Neue Spalten fuer Angaben aus dem Altsystem
ALTER TABLE kunde ADD COLUMN IF NOT EXISTS kundentyp  VARCHAR(10);
ALTER TABLE kunde ADD COLUMN IF NOT EXISTS kunde_seit DATE;
ALTER TABLE kunde ADD COLUMN IF NOT EXISTS bemerkung  VARCHAR(500);

-- vorhandene Kunden einordnen: mit Firma = Gewerbekunde, sonst Privatkunde
UPDATE kunde SET kundentyp = CASE WHEN firma IS NULL THEN 'PRIVAT' ELSE 'GEWERBE' END
 WHERE kundentyp IS NULL;
ALTER TABLE kunde ALTER COLUMN kundentyp SET NOT NULL;
ALTER TABLE kunde DROP CONSTRAINT IF EXISTS ck_kunde_kundentyp;
ALTER TABLE kunde ADD CONSTRAINT ck_kunde_kundentyp CHECK (kundentyp IN ('PRIVAT', 'GEWERBE'));

-- 3. Der Beispielkunde aus data.sql belegt K-1001 - im Altsystem gehoert diese Nummer "GaLaBau Krause".
--    Deshalb umnummern. Seine Reservierungen bleiben erhalten (verknuepft ueber die ID, nicht die Nummer).
UPDATE kunde SET kundennummer = 'K-0001'
 WHERE kundennummer = 'K-1001' AND email = 'max@mustermann-bau.de';
