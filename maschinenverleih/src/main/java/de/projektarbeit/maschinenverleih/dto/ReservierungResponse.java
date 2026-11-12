package de.projektarbeit.maschinenverleih.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import de.projektarbeit.maschinenverleih.model.Reservierung;
import de.projektarbeit.maschinenverleih.model.ReservierungsStatus;

public record ReservierungResponse(
        Long id,
        Long kundeId,
        String kunde,
        Long maschineId,
        String maschine,
        String standort,
        LocalDate von,
        LocalDate bis,
        long mietTage,
        BigDecimal tagesmietpreis,
        BigDecimal voraussichtlicherPreis,
        ReservierungsStatus status,
        LocalDateTime erstelltAm) {

    public static ReservierungResponse von(Reservierung r) {
        var maschine = r.getMaschine();
        // von und bis zaehlen beide als Miettag
        long tage = ChronoUnit.DAYS.between(r.getVon(), r.getBis()) + 1;
        BigDecimal preis = maschine.getTagesmietpreis().multiply(BigDecimal.valueOf(tage));
        return new ReservierungResponse(r.getId(), r.getKunde().getId(), r.getKunde().getName(),
                maschine.getId(), maschine.getBezeichnung(), maschine.getStandort().getName(),
                r.getVon(), r.getBis(), tage, maschine.getTagesmietpreis(), preis, r.getStatus(),
                r.getErstelltAm());
    }
}
