package de.projektarbeit.maschinenverleih.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KategorieRequest(
        @Schema(example = "Teleskoplader")
        @NotBlank(message = "Name ist ein Pflichtfeld")
        @Size(max = 50, message = "Name darf höchstens 50 Zeichen lang sein")
        String name) {
}
