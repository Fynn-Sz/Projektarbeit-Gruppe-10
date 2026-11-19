package de.projektarbeit.maschinenverleih.dto;

import java.util.List;

/**
 * Protokoll eines Kunden-Imports aus den Altdaten.
 *
 * @param importiert       neu angelegte Kunden
 * @param bereitsVorhanden Zeilen, deren Kunde schon da ist (z. B. beim zweiten Importlauf) - nichts zu tun
 * @param dubletten        nicht angelegt, weil der Kunde doppelt vorkommt (mit Verweis auf den vorhandenen Kunden)
 * @param fehler           nicht angelegt, weil fehlerhaft, unvollstaendig oder unklar - muss jemand pruefen
 * @param hinweise         angelegt, aber mit Korrekturen oder Annahmen (z. B. Ort korrigiert, Nummer vergeben)
 */
public record KundenImportErgebnis(
        int importiert,
        int bereitsVorhanden,
        List<Zeilenmeldung> dubletten,
        List<Zeilenmeldung> fehler,
        List<Zeilenmeldung> hinweise) {

    /** @param zeile Zeilennummer wie in Excel angezeigt */
    public record Zeilenmeldung(int zeile, String kundennummer, String meldung) {
    }
}
