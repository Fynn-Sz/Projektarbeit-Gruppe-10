-- Datenbankschema: wird beim Start ausgefuehrt und legt Schema und Tabellen explizit an.
-- "IF NOT EXISTS" sorgt dafuer, dass vorhandene Tabellen und Daten erhalten bleiben.

CREATE SCHEMA IF NOT EXISTS maschinenverleih;

CREATE TABLE IF NOT EXISTS kategorie (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name    VARCHAR(50) NOT NULL,
    CONSTRAINT uq_kategorie_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS standort (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    strasse VARCHAR(100),
    plz     VARCHAR(5),
    ort     VARCHAR(100),
    CONSTRAINT uq_standort_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS maschine (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    inventarnummer          VARCHAR(20)   NOT NULL,
    bezeichnung             VARCHAR(100)  NOT NULL,
    kategorie_id            BIGINT        NOT NULL,
    standort_id             BIGINT        NOT NULL,
    hersteller              VARCHAR(50),
    baujahr                 INTEGER       NOT NULL,
    betriebsstunden         INTEGER       NOT NULL DEFAULT 0,
    wartungsintervall_std   INTEGER       NOT NULL,
    tagesmietpreis          NUMERIC(10,2) NOT NULL,
    garantie_bis            DATE,
    status                  VARCHAR(20)   NOT NULL DEFAULT 'VERFUEGBAR',
    CONSTRAINT uq_maschine_inventarnummer UNIQUE (inventarnummer),
    CONSTRAINT fk_maschine_kategorie FOREIGN KEY (kategorie_id) REFERENCES kategorie (id),
    CONSTRAINT fk_maschine_standort  FOREIGN KEY (standort_id)  REFERENCES standort (id),
    CONSTRAINT ck_maschine_status CHECK (status IN ('VERFUEGBAR', 'IN_WARTUNG', 'AUSSER_BETRIEB')),
    CONSTRAINT ck_maschine_baujahr CHECK (baujahr >= 1950),
    CONSTRAINT ck_maschine_betriebsstunden CHECK (betriebsstunden >= 0),
    CONSTRAINT ck_maschine_wartungsintervall CHECK (wartungsintervall_std > 0),
    CONSTRAINT ck_maschine_tagesmietpreis CHECK (tagesmietpreis >= 0)
);

CREATE TABLE IF NOT EXISTS kunde (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kundennummer    VARCHAR(20)  NOT NULL,
    firma           VARCHAR(100),
    vorname         VARCHAR(50)  NOT NULL,
    nachname        VARCHAR(50)  NOT NULL,
    email           VARCHAR(100) NOT NULL,
    telefon         VARCHAR(30),
    strasse         VARCHAR(100) NOT NULL,
    plz             VARCHAR(5)   NOT NULL,
    ort             VARCHAR(100) NOT NULL,
    ust_id          VARCHAR(20),
    erstellt_am     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_kunde_kundennummer UNIQUE (kundennummer),
    CONSTRAINT uq_kunde_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS reservierung (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kunde_id        BIGINT      NOT NULL,
    maschine_id     BIGINT      NOT NULL,
    von             DATE        NOT NULL,
    bis             DATE        NOT NULL,
    status          VARCHAR(20) NOT NULL,
    erstellt_am     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservierung_kunde    FOREIGN KEY (kunde_id)    REFERENCES kunde (id),
    CONSTRAINT fk_reservierung_maschine FOREIGN KEY (maschine_id) REFERENCES maschine (id),
    CONSTRAINT ck_reservierung_zeitraum CHECK (bis >= von),
    CONSTRAINT ck_reservierung_status CHECK (status IN ('ANGEFRAGT', 'BESTAETIGT', 'STORNIERT'))
);

-- beschleunigt die Verfuegbarkeitssuche (Ueberschneidung von Zeitraeumen je Maschine)
CREATE INDEX IF NOT EXISTS ix_reservierung_maschine_zeitraum ON reservierung (maschine_id, von, bis);

-- Audit-Trail. "benutzer" ist vorerst Text; mit dem Login (US 8) wird daraus ein Fremdschluessel auf benutzer.
CREATE TABLE IF NOT EXISTS audit_log (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    zeitpunkt       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    benutzer        VARCHAR(100) NOT NULL,
    aktion          VARCHAR(20)  NOT NULL,
    tabelle         VARCHAR(50)  NOT NULL,
    datensatz_id    BIGINT       NOT NULL,
    alte_werte      TEXT,
    neue_werte      TEXT,
    CONSTRAINT ck_audit_log_aktion CHECK (aktion IN ('ANGELEGT', 'GEAENDERT', 'GELOESCHT', 'IMPORTIERT'))
);

CREATE INDEX IF NOT EXISTS ix_audit_log_datensatz ON audit_log (tabelle, datensatz_id);
