package de.projektarbeit.maschinenverleih.dto;

import java.util.List;

public record ImportErgebnis(int importiert, int uebersprungen, List<ImportFehler> fehler) {

    public record ImportFehler(int zeile, String meldung) {
    }
}
