package de.projektarbeit.maschinenverleih.model;

public enum Kundentyp {
    PRIVAT("Privatkunde"),
    GEWERBE("Gewerbekunde");

    // Anzeigename fuer die Weboberflaeche (in der API und in der Datenbank steht der Name der Konstante)
    private final String bezeichnung;

    Kundentyp(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}
