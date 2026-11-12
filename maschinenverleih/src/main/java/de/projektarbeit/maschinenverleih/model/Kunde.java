package de.projektarbeit.maschinenverleih.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
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
    private String firma;
    private String vorname;
    private String nachname;
    private String email;
    private String telefon;
    private String strasse;
    private String plz;
    private String ort;
    private String ustId;
    private LocalDateTime erstelltAm;

    public Kunde() {
    }

    @PrePersist
    void vorDemSpeichern() {
        erstelltAm = LocalDateTime.now();
    }

    public String getName() {
        return vorname + " " + nachname;
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

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
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

    public LocalDateTime getErstelltAm() {
        return erstelltAm;
    }
}
