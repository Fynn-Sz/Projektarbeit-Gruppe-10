package de.projektarbeit.maschinenverleih.service.kundenimport;

/**
 * Eine Kundenzeile aus dem alten Excel-System - unveraendert als Text, so wie sie in der Zelle steht.
 *
 * @param zeile Zeilennummer wie in Excel angezeigt (fuer das Protokoll)
 */
public record AltkundeRoh(
        int zeile,
        String kundennummer,
        String name,
        String adresse,
        String telefon,
        String email,
        String kundentyp,
        String kundeSeit,
        String anmerkung) {
}
