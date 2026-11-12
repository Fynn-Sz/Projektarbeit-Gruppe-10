package de.projektarbeit.maschinenverleih.model;

public enum AuditAktion {
    ANGELEGT("Angelegt"),
    GEAENDERT("Geändert"),
    GELOESCHT("Gelöscht"),
    IMPORTIERT("Importiert");

    // Anzeigename fuer die Weboberflaeche (in der API bleibt der Name der Konstante)
    private final String bezeichnung;

    AuditAktion(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}
