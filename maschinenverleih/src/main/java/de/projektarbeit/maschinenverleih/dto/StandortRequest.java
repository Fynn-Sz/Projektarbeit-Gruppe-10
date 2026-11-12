package de.projektarbeit.maschinenverleih.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StandortRequest(
        @Schema(example = "Herford")
        @NotBlank(message = "Name ist ein Pflichtfeld")
        @Size(max = 100, message = "Name darf höchstens 100 Zeichen lang sein")
        String name,

        @Schema(example = "Lagerweg 3")
        @Size(max = 100, message = "Straße darf höchstens 100 Zeichen lang sein")
        String strasse,

        @Schema(example = "32052")
        @Pattern(regexp = "\\d{5}", message = "PLZ muss aus 5 Ziffern bestehen")
        String plz,

        @Schema(example = "Herford")
        @Size(max = 100, message = "Ort darf höchstens 100 Zeichen lang sein")
        String ort) {
}
