package de.projektarbeit.maschinenverleih.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import de.projektarbeit.maschinenverleih.model.Anrede;
import de.projektarbeit.maschinenverleih.model.Kunde;
import de.projektarbeit.maschinenverleih.model.Kundentyp;

public record KundeResponse(
        Long id,
        String kundennummer,
        Kundentyp kundentyp,
        String firma,
        Anrede anrede,
        String vorname,
        String nachname,
        String anzeigename,
        String briefanrede,
        String email,
        String telefon,
        String strasse,
        String plz,
        String ort,
        String ustId,
        LocalDate kundeSeit,
        String bemerkung,
        LocalDateTime erstelltAm) {

    public static KundeResponse von(Kunde k) {
        return new KundeResponse(k.getId(), k.getKundennummer(), k.getKundentyp(), k.getFirma(), k.getAnrede(),
                k.getVorname(), k.getNachname(), k.getAnzeigename(), k.getBriefanrede(), k.getEmail(), k.getTelefon(), k.getStrasse(), k.getPlz(),
                k.getOrt(), k.getUstId(), k.getKundeSeit(), k.getBemerkung(), k.getErstelltAm());
    }
}
