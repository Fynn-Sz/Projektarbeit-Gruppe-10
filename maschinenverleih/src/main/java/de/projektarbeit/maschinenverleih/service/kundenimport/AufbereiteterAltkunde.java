package de.projektarbeit.maschinenverleih.service.kundenimport;

import java.util.List;

import de.projektarbeit.maschinenverleih.dto.KundeRequest;

/**
 * Ergebnis der Aufbereitung einer Altdaten-Zeile.
 *
 * @param request  die Felder im neuen Datenmodell - null, wenn die Zeile Fehler hat
 * @param hinweise Korrekturen, die beim Aufbereiten gemacht wurden (z. B. Ort "Remschied" -> "Remscheid")
 * @param fehler   Gruende, warum die Zeile so nicht uebernommen werden kann
 */
public record AufbereiteterAltkunde(int zeile, String kundennummerRoh, KundeRequest request,
        List<String> hinweise, List<String> fehler) {

    public boolean hatFehler() {
        return !fehler.isEmpty();
    }

    public boolean ohneKundennummer() {
        return request != null && request.kundennummer() == null;
    }
}
