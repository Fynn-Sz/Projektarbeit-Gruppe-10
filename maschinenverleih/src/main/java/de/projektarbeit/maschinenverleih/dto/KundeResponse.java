package de.projektarbeit.maschinenverleih.dto;

import java.time.LocalDateTime;

import de.projektarbeit.maschinenverleih.model.Kunde;

public record KundeResponse(
        Long id,
        String kundennummer,
        String firma,
        String vorname,
        String nachname,
        String email,
        String telefon,
        String strasse,
        String plz,
        String ort,
        String ustId,
        LocalDateTime erstelltAm) {

    public static KundeResponse von(Kunde k) {
        return new KundeResponse(k.getId(), k.getKundennummer(), k.getFirma(), k.getVorname(), k.getNachname(),
                k.getEmail(), k.getTelefon(), k.getStrasse(), k.getPlz(), k.getOrt(), k.getUstId(),
                k.getErstelltAm());
    }
}
