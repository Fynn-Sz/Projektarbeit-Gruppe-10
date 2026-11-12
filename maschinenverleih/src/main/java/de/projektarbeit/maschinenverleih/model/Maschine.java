package de.projektarbeit.maschinenverleih.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "maschine")
public class Maschine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String inventarnummer;
    private String bezeichnung;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kategorie_id")
    private Kategorie kategorie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "standort_id")
    private Standort standort;

    private String hersteller;
    private Integer baujahr;
    private Integer betriebsstunden;
    private Integer wartungsintervallStd;
    private BigDecimal tagesmietpreis;
    private LocalDate garantieBis;

    @Enumerated(EnumType.STRING)
    private MaschinenStatus status = MaschinenStatus.VERFUEGBAR;

    public Maschine() {
    }

    public Long getId() {
        return id;
    }

    public String getInventarnummer() {
        return inventarnummer;
    }

    public void setInventarnummer(String inventarnummer) {
        this.inventarnummer = inventarnummer;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public Kategorie getKategorie() {
        return kategorie;
    }

    public void setKategorie(Kategorie kategorie) {
        this.kategorie = kategorie;
    }

    public Standort getStandort() {
        return standort;
    }

    public void setStandort(Standort standort) {
        this.standort = standort;
    }

    public String getHersteller() {
        return hersteller;
    }

    public void setHersteller(String hersteller) {
        this.hersteller = hersteller;
    }

    public Integer getBaujahr() {
        return baujahr;
    }

    public void setBaujahr(Integer baujahr) {
        this.baujahr = baujahr;
    }

    public Integer getBetriebsstunden() {
        return betriebsstunden;
    }

    public void setBetriebsstunden(Integer betriebsstunden) {
        this.betriebsstunden = betriebsstunden;
    }

    public Integer getWartungsintervallStd() {
        return wartungsintervallStd;
    }

    public void setWartungsintervallStd(Integer wartungsintervallStd) {
        this.wartungsintervallStd = wartungsintervallStd;
    }

    public BigDecimal getTagesmietpreis() {
        return tagesmietpreis;
    }

    public void setTagesmietpreis(BigDecimal tagesmietpreis) {
        this.tagesmietpreis = tagesmietpreis;
    }

    public LocalDate getGarantieBis() {
        return garantieBis;
    }

    public void setGarantieBis(LocalDate garantieBis) {
        this.garantieBis = garantieBis;
    }

    public MaschinenStatus getStatus() {
        return status;
    }

    public void setStatus(MaschinenStatus status) {
        this.status = status;
    }
}
