package de.projektarbeit.maschinenverleih.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record ReservierungRequest(
        @Schema(example = "1", description = "Bis zum Login (US 8) wird der Kunde direkt mitgegeben")
        @NotNull(message = "Kunde ist ein Pflichtfeld")
        Long kundeId,

        @Schema(example = "1")
        @NotNull(message = "Maschine ist ein Pflichtfeld")
        Long maschineId,

        @Schema(example = "2026-12-01")
        @NotNull(message = "Startdatum ist ein Pflichtfeld")
        @FutureOrPresent(message = "Startdatum darf nicht in der Vergangenheit liegen")
        LocalDate von,

        @Schema(example = "2026-12-05")
        @NotNull(message = "Enddatum ist ein Pflichtfeld")
        LocalDate bis) {
}
