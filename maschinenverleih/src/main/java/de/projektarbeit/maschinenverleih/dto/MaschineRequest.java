package de.projektarbeit.maschinenverleih.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import de.projektarbeit.maschinenverleih.model.MaschinenStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MaschineRequest(
        @Schema(example = "M-0100")
        @NotBlank(message = "Inventarnummer ist ein Pflichtfeld")
        @Size(max = 20, message = "Inventarnummer darf höchstens 20 Zeichen lang sein")
        String inventarnummer,

        @Schema(example = "Radlader Volvo L30")
        @NotBlank(message = "Bezeichnung ist ein Pflichtfeld")
        @Size(max = 100, message = "Bezeichnung darf höchstens 100 Zeichen lang sein")
        String bezeichnung,

        @Schema(example = "4", description = "ID aus GET /api/kategorien")
        @NotNull(message = "Kategorie ist ein Pflichtfeld")
        Long kategorieId,

        @Schema(example = "1", description = "ID aus GET /api/standorte")
        @NotNull(message = "Standort ist ein Pflichtfeld")
        Long standortId,

        @Schema(example = "Volvo")
        @Size(max = 50, message = "Hersteller darf höchstens 50 Zeichen lang sein")
        String hersteller,

        @Schema(example = "2020")
        @NotNull(message = "Baujahr ist ein Pflichtfeld")
        @Min(value = 1950, message = "Baujahr muss ab 1950 sein")
        Integer baujahr,

        @Schema(example = "800")
        @NotNull(message = "Betriebsstunden sind ein Pflichtfeld")
        @Min(value = 0, message = "Betriebsstunden dürfen nicht negativ sein")
        Integer betriebsstunden,

        @Schema(example = "500", description = "Wartung alle X Betriebsstunden")
        @NotNull(message = "Wartungsintervall ist ein Pflichtfeld")
        @Min(value = 1, message = "Wartungsintervall muss mindestens 1 Stunde sein")
        Integer wartungsintervallStd,

        @Schema(example = "95.00")
        @NotNull(message = "Tagesmietpreis ist ein Pflichtfeld")
        @DecimalMin(value = "0.00", message = "Tagesmietpreis darf nicht negativ sein")
        @Digits(integer = 8, fraction = 2, message = "Tagesmietpreis: höchstens 2 Nachkommastellen")
        BigDecimal tagesmietpreis,

        @Schema(example = "2027-12-31")
        LocalDate garantieBis,

        @Schema(description = "Standard: VERFUEGBAR")
        MaschinenStatus status) {
}
