package de.projektarbeit.maschinenverleih.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record KundeRequest(
        @Schema(example = "K-1002")
        @NotBlank(message = "Kundennummer ist ein Pflichtfeld")
        @Size(max = 20, message = "Kundennummer darf höchstens 20 Zeichen lang sein")
        String kundennummer,

        @Schema(example = "Schmidt Gartenbau")
        @Size(max = 100, message = "Firma darf höchstens 100 Zeichen lang sein")
        String firma,

        @Schema(example = "Erika")
        @NotBlank(message = "Vorname ist ein Pflichtfeld")
        @Size(max = 50, message = "Vorname darf höchstens 50 Zeichen lang sein")
        String vorname,

        @Schema(example = "Schmidt")
        @NotBlank(message = "Nachname ist ein Pflichtfeld")
        @Size(max = 50, message = "Nachname darf höchstens 50 Zeichen lang sein")
        String nachname,

        @Schema(example = "erika@schmidt-gartenbau.de")
        @NotBlank(message = "E-Mail ist ein Pflichtfeld")
        @Email(message = "E-Mail-Adresse ist ungültig")
        @Size(max = 100, message = "E-Mail darf höchstens 100 Zeichen lang sein")
        String email,

        @Schema(example = "05221 98765")
        @Size(max = 30, message = "Telefon darf höchstens 30 Zeichen lang sein")
        String telefon,

        @Schema(example = "Gartenweg 7")
        @NotBlank(message = "Straße ist ein Pflichtfeld")
        @Size(max = 100, message = "Straße darf höchstens 100 Zeichen lang sein")
        String strasse,

        @Schema(example = "32049")
        @NotBlank(message = "PLZ ist ein Pflichtfeld")
        @Pattern(regexp = "\\d{5}", message = "PLZ muss aus 5 Ziffern bestehen")
        String plz,

        @Schema(example = "Herford")
        @NotBlank(message = "Ort ist ein Pflichtfeld")
        @Size(max = 100, message = "Ort darf höchstens 100 Zeichen lang sein")
        String ort,

        @Schema(example = "DE987654321")
        @Size(max = 20, message = "USt-ID darf höchstens 20 Zeichen lang sein")
        String ustId) {
}
