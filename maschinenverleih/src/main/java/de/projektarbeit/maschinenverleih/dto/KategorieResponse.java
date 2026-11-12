package de.projektarbeit.maschinenverleih.dto;

import de.projektarbeit.maschinenverleih.model.Kategorie;

public record KategorieResponse(Long id, String name) {

    public static KategorieResponse von(Kategorie kategorie) {
        return new KategorieResponse(kategorie.getId(), kategorie.getName());
    }
}
