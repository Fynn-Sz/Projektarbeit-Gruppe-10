package de.projektarbeit.maschinenverleih.web;

import de.projektarbeit.maschinenverleih.dto.KundeRequest;

/** Formularobjekt fuer "Kunde anlegen" - Pruefregeln stehen im KundeRequest. */
public class KundeFormular {

    private String kundennummer;
    private String firma;
    private String vorname;
    private String nachname;
    private String email;
    private String telefon;
    private String strasse;
    private String plz;
    private String ort;
    private String ustId;

    public KundeRequest alsRequest() {
        return new KundeRequest(kundennummer, leerZuNull(firma), vorname, nachname, email, leerZuNull(telefon),
                strasse, plz, ort, leerZuNull(ustId));
    }

    private static String leerZuNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    public String getKundennummer() { return kundennummer; }
    public void setKundennummer(String kundennummer) { this.kundennummer = kundennummer; }
    public String getFirma() { return firma; }
    public void setFirma(String firma) { this.firma = firma; }
    public String getVorname() { return vorname; }
    public void setVorname(String vorname) { this.vorname = vorname; }
    public String getNachname() { return nachname; }
    public void setNachname(String nachname) { this.nachname = nachname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }
    public String getStrasse() { return strasse; }
    public void setStrasse(String strasse) { this.strasse = strasse; }
    public String getPlz() { return plz; }
    public void setPlz(String plz) { this.plz = plz; }
    public String getOrt() { return ort; }
    public void setOrt(String ort) { this.ort = ort; }
    public String getUstId() { return ustId; }
    public void setUstId(String ustId) { this.ustId = ustId; }
}
