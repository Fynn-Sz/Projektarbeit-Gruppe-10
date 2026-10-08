package de.projektarbeit.maschinenverleih.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "maschine")
public class Maschine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Bezeichnung ist ein Pflichtfeld")
    private String bezeichnung;

    @NotBlank(message = "Typ ist ein Pflichtfeld")
    private String typ;

    @NotNull(message = "Baujahr ist ein Pflichtfeld")
    @Min(value = 1950, message = "Baujahr muss ab 1950 sein")
    private Integer baujahr;

    @Min(value = 0, message = "Betriebsstunden duerfen nicht negativ sein")
    private int betriebsstunden;

    @NotNull(message = "Wartungsintervall ist ein Pflichtfeld")
    @Min(value = 1, message = "Wartungsintervall muss mindestens 1 Stunde sein")
    private Integer wartungsintervall;

    private String standort;

    // JPA braucht einen leeren Konstruktor
    public Maschine() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public Integer getBaujahr() {
        return baujahr;
    }

    public void setBaujahr(Integer baujahr) {
        this.baujahr = baujahr;
    }

    public int getBetriebsstunden() {
        return betriebsstunden;
    }

    public void setBetriebsstunden(int betriebsstunden) {
        this.betriebsstunden = betriebsstunden;
    }

    public Integer getWartungsintervall() {
        return wartungsintervall;
    }

    public void setWartungsintervall(Integer wartungsintervall) {
        this.wartungsintervall = wartungsintervall;
    }

    public String getStandort() {
        return standort;
    }

    public void setStandort(String standort) {
        this.standort = standort;
    }
}
