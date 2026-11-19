package de.projektarbeit.maschinenverleih.model;

/**
 * Anrede eines Kunden bzw. des Ansprechpartners einer Firma. Optional - ohne Anrede wird neutral angeschrieben.
 * In der Datenbank steht der Name der Konstante (FRAU/HERR), nicht die Position im Enum.
 */
public enum Anrede {
    FRAU("Frau", "Sehr geehrte Frau"),
    HERR("Herr", "Sehr geehrter Herr");

    private final String bezeichnung;
    private final String briefanfang;

    Anrede(String bezeichnung, String briefanfang) {
        this.bezeichnung = bezeichnung;
        this.briefanfang = briefanfang;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    /** "Sehr geehrte Frau" - der Nachname wird in Kunde.getBriefanrede() angehaengt. */
    public String getBriefanfang() {
        return briefanfang;
    }
}
