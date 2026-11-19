package de.projektarbeit.maschinenverleih.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import de.projektarbeit.maschinenverleih.dto.KundeRequest;
import de.projektarbeit.maschinenverleih.dto.KundeResponse;
import de.projektarbeit.maschinenverleih.model.Anrede;
import de.projektarbeit.maschinenverleih.model.Kundentyp;

/** Formularobjekt fuer "Kunde anlegen/bearbeiten" - Pruefregeln stehen im KundeRequest und KundeService. */
public class KundeFormular {

    private String kundennummer;
    private Kundentyp kundentyp = Kundentyp.PRIVAT;
    private String firma;
    private Anrede anrede;
    private String vorname;
    private String nachname;
    private String email;
    private String telefon;
    private String strasse;
    private String plz;
    private String ort;
    private String ustId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate kundeSeit;
    private String bemerkung;

    public static KundeFormular von(KundeResponse k) {
        var f = new KundeFormular();
        f.kundennummer = k.kundennummer();
        f.kundentyp = k.kundentyp();
        f.firma = k.firma();
        f.anrede = k.anrede();
        f.vorname = k.vorname();
        f.nachname = k.nachname();
        f.email = k.email();
        f.telefon = k.telefon();
        f.strasse = k.strasse();
        f.plz = k.plz();
        f.ort = k.ort();
        f.ustId = k.ustId();
        f.kundeSeit = k.kundeSeit();
        f.bemerkung = k.bemerkung();
        return f;
    }

    public KundeRequest alsRequest() {
        return new KundeRequest(kundennummer, kundentyp, leerZuNull(firma), anrede, leerZuNull(vorname), nachname,
                leerZuNull(email), leerZuNull(telefon), strasse, plz, ort, leerZuNull(ustId), kundeSeit,
                leerZuNull(bemerkung));
    }

    private static String leerZuNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    public String getKundennummer() { return kundennummer; }
    public void setKundennummer(String kundennummer) { this.kundennummer = kundennummer; }
    public Kundentyp getKundentyp() { return kundentyp; }
    public void setKundentyp(Kundentyp kundentyp) { this.kundentyp = kundentyp; }
    public String getFirma() { return firma; }
    public void setFirma(String firma) { this.firma = firma; }
    public Anrede getAnrede() { return anrede; }
    public void setAnrede(Anrede anrede) { this.anrede = anrede; }
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
    public LocalDate getKundeSeit() { return kundeSeit; }
    public void setKundeSeit(LocalDate kundeSeit) { this.kundeSeit = kundeSeit; }
    public String getBemerkung() { return bemerkung; }
    public void setBemerkung(String bemerkung) { this.bemerkung = bemerkung; }
}
