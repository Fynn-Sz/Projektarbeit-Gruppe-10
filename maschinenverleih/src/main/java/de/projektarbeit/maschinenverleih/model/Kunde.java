package de.projektarbeit.maschinenverleih.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "kunde")
public class Kunde {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kundennummer;

    @Enumerated(EnumType.STRING)
    private Kundentyp kundentyp;

    private String firma;

    @Enumerated(EnumType.STRING)
    private Anrede anrede;

    private String vorname;
    private String nachname;
    private String email;
    private String telefon;
    private String strasse;
    private String plz;
    private String ort;
    private String ustId;
    private LocalDate kundeSeit;
    private String bemerkung;
    private LocalDateTime erstelltAm;

    public Kunde() {
    }

    @PrePersist
    void vorDemSpeichern() {
        erstelltAm = LocalDateTime.now();
    }

    /** "Erika Schmidt" - bei Firmen-Ansprechpartnern ohne Vornamen nur "Schmidt". */
    public String getName() {
        return vorname == null ? nachname : vorname + " " + nachname;
    }

    /** Wie der Kunde in Listen erscheint: Firmen mit Firmennamen, Privatkunden mit ihrem Namen. */
    public String getAnzeigename() {
        return firma != null ? firma + " (" + getName() + ")" : getName();
    }

    /**
     * Anrede fuer Briefe und E-Mails: "Sehr geehrte Frau Krause", "Sehr geehrter Herr Klein".
     * Ohne Anrede neutral: Privatkunde "Guten Tag Birgit Krause", Firma "Sehr geehrte Damen und Herren".
     */
    public String getBriefanrede() {
        if (anrede != null) {
            return anrede.getBriefanfang() + " " + nachname;
        }
        return vorname != null ? "Guten Tag " + vorname + " " + nachname : "Sehr geehrte Damen und Herren";
    }

    public Long getId() {
        return id;
    }

    public String getKundennummer() {
        return kundennummer;
    }

    public void setKundennummer(String kundennummer) {
        this.kundennummer = kundennummer;
    }

    public Kundentyp getKundentyp() {
        return kundentyp;
    }

    public void setKundentyp(Kundentyp kundentyp) {
        this.kundentyp = kundentyp;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }

    public Anrede getAnrede() {
        return anrede;
    }

    public void setAnrede(Anrede anrede) {
        this.anrede = anrede;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public String getUstId() {
        return ustId;
    }

    public void setUstId(String ustId) {
        this.ustId = ustId;
    }

    public LocalDate getKundeSeit() {
        return kundeSeit;
    }

    public void setKundeSeit(LocalDate kundeSeit) {
        this.kundeSeit = kundeSeit;
    }

    public String getBemerkung() {
        return bemerkung;
    }

    public void setBemerkung(String bemerkung) {
        this.bemerkung = bemerkung;
    }

    public LocalDateTime getErstelltAm() {
        return erstelltAm;
    }
}
