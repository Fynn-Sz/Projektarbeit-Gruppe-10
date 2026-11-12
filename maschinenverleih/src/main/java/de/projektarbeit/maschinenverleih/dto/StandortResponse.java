package de.projektarbeit.maschinenverleih.dto;

import de.projektarbeit.maschinenverleih.model.Standort;

public record StandortResponse(Long id, String name, String strasse, String plz, String ort) {

    public static StandortResponse von(Standort standort) {
        return new StandortResponse(standort.getId(), standort.getName(), standort.getStrasse(),
                standort.getPlz(), standort.getOrt());
    }
}
