package de.projektarbeit.maschinenverleih.model;

public enum MaschinenStatus {
    VERFUEGBAR("Verfügbar"),
    IN_WARTUNG("In Wartung"),
    AUSSER_BETRIEB("Außer Betrieb");

    // Anzeigename fuer die Weboberflaeche (in der API bleibt der Name der Konstante)
    private final String bezeichnung;

    MaschinenStatus(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}
