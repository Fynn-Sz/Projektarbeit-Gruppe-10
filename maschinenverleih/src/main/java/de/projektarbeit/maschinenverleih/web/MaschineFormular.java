package de.projektarbeit.maschinenverleih.web;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import de.projektarbeit.maschinenverleih.dto.MaschineRequest;
import de.projektarbeit.maschinenverleih.dto.MaschineResponse;
import de.projektarbeit.maschinenverleih.model.MaschinenStatus;

/**
 * Formularobjekt fuer "Maschine anlegen/bearbeiten". Thymeleaf braucht Getter und Setter,
 * deshalb eine eigene Klasse statt des Records. Die Pruefregeln stehen weiterhin nur im
 * MaschineRequest - das Formular wird vor dem Speichern in einen Request umgewandelt.
 */
public class MaschineFormular {

    private String inventarnummer;
    private String bezeichnung;
    private Long kategorieId;
    private Long standortId;
    private String hersteller;
    private Integer baujahr;
    private Integer betriebsstunden;
    private Integer wartungsintervallStd;
    private BigDecimal tagesmietpreis;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate garantieBis;
    private MaschinenStatus status = MaschinenStatus.VERFUEGBAR;

    public static MaschineFormular von(MaschineResponse m) {
        var f = new MaschineFormular();
        f.inventarnummer = m.inventarnummer();
        f.bezeichnung = m.bezeichnung();
        f.kategorieId = m.kategorieId();
        f.standortId = m.standortId();
        f.hersteller = m.hersteller();
        f.baujahr = m.baujahr();
        f.betriebsstunden = m.betriebsstunden();
        f.wartungsintervallStd = m.wartungsintervallStd();
        f.tagesmietpreis = m.tagesmietpreis();
        f.garantieBis = m.garantieBis();
        f.status = m.status();
        return f;
    }

    public MaschineRequest alsRequest() {
        return new MaschineRequest(inventarnummer, bezeichnung, kategorieId, standortId, leerZuNull(hersteller),
                baujahr, betriebsstunden, wartungsintervallStd, tagesmietpreis, garantieBis, status);
    }

    private static String leerZuNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    public String getInventarnummer() { return inventarnummer; }
    public void setInventarnummer(String inventarnummer) { this.inventarnummer = inventarnummer; }
    public String getBezeichnung() { return bezeichnung; }
    public void setBezeichnung(String bezeichnung) { this.bezeichnung = bezeichnung; }
    public Long getKategorieId() { return kategorieId; }
    public void setKategorieId(Long kategorieId) { this.kategorieId = kategorieId; }
    public Long getStandortId() { return standortId; }
    public void setStandortId(Long standortId) { this.standortId = standortId; }
    public String getHersteller() { return hersteller; }
    public void setHersteller(String hersteller) { this.hersteller = hersteller; }
    public Integer getBaujahr() { return baujahr; }
    public void setBaujahr(Integer baujahr) { this.baujahr = baujahr; }
    public Integer getBetriebsstunden() { return betriebsstunden; }
    public void setBetriebsstunden(Integer betriebsstunden) { this.betriebsstunden = betriebsstunden; }
    public Integer getWartungsintervallStd() { return wartungsintervallStd; }
    public void setWartungsintervallStd(Integer wartungsintervallStd) { this.wartungsintervallStd = wartungsintervallStd; }
    public BigDecimal getTagesmietpreis() { return tagesmietpreis; }
    public void setTagesmietpreis(BigDecimal tagesmietpreis) { this.tagesmietpreis = tagesmietpreis; }
    public LocalDate getGarantieBis() { return garantieBis; }
    public void setGarantieBis(LocalDate garantieBis) { this.garantieBis = garantieBis; }
    public MaschinenStatus getStatus() { return status; }
    public void setStatus(MaschinenStatus status) { this.status = status; }
}
