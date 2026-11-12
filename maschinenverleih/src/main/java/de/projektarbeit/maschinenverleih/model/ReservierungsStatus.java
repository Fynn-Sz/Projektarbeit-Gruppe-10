package de.projektarbeit.maschinenverleih.model;

public enum ReservierungsStatus {
    ANGEFRAGT("Angefragt"),
    BESTAETIGT("Bestätigt"),
    STORNIERT("Storniert");

    // Anzeigename fuer die Weboberflaeche (in der API bleibt der Name der Konstante)
    private final String bezeichnung;

    ReservierungsStatus(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}
