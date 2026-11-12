package de.projektarbeit.maschinenverleih.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import de.projektarbeit.maschinenverleih.model.Maschine;
import de.projektarbeit.maschinenverleih.model.MaschinenStatus;

public record MaschineResponse(
        Long id,
        String inventarnummer,
        String bezeichnung,
        Long kategorieId,
        String kategorie,
        Long standortId,
        String standort,
        String hersteller,
        Integer baujahr,
        Integer betriebsstunden,
        Integer wartungsintervallStd,
        BigDecimal tagesmietpreis,
        LocalDate garantieBis,
        MaschinenStatus status) {

    public static MaschineResponse von(Maschine m) {
        return new MaschineResponse(m.getId(), m.getInventarnummer(), m.getBezeichnung(),
                m.getKategorie().getId(), m.getKategorie().getName(),
                m.getStandort().getId(), m.getStandort().getName(),
                m.getHersteller(), m.getBaujahr(), m.getBetriebsstunden(), m.getWartungsintervallStd(),
                m.getTagesmietpreis(), m.getGarantieBis(), m.getStatus());
    }
}
