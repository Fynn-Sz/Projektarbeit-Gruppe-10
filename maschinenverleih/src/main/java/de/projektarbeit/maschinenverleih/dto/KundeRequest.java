package de.projektarbeit.maschinenverleih.dto;

import java.time.LocalDate;

import de.projektarbeit.maschinenverleih.model.Anrede;
import de.projektarbeit.maschinenverleih.model.Kundentyp;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Eingaben fuer "Kunde anlegen/aendern" - genutzt von REST-API, Webformular und Excel-Import.
 * Regeln, die mehrere Felder betreffen (z. B. "Gewerbekunde braucht eine Firma"), prueft der KundeService.
 */
public record KundeRequest(
        @Schema(example = "K-1002")
        @NotBlank(message = "Kundennummer ist ein Pflichtfeld")
        @Size(max = 20, message = "Kundennummer darf höchstens 20 Zeichen lang sein")
        String kundennummer,

        @Schema(example = "GEWERBE")
        @NotNull(message = "Kundentyp ist ein Pflichtfeld")
        Kundentyp kundentyp,

        @Schema(example = "Schmidt Gartenbau")
        @Size(max = 100, message = "Firma darf höchstens 100 Zeichen lang sein")
        String firma,

        @Schema(example = "FRAU", description = "Anrede des Kunden bzw. Ansprechpartners, optional (leer = neutral)")
        Anrede anrede,

        @Schema(example = "Erika", description = "Pflicht bei Privatkunden, bei Firmen-Ansprechpartnern optional")
        @Size(max = 50, message = "Vorname darf höchstens 50 Zeichen lang sein")
        String vorname,

        @Schema(example = "Schmidt")
        @NotBlank(message = "Nachname ist ein Pflichtfeld")
        @Size(max = 50, message = "Nachname darf höchstens 50 Zeichen lang sein")
        String nachname,

        @Schema(example = "erika@schmidt-gartenbau.de", description = "E-Mail oder Telefon muss angegeben sein")
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
        String ustId,

        @Schema(example = "2016-06-21", description = "Seit wann der Kunde bei uns ist (aus dem Altsystem)")
        @PastOrPresent(message = "Kunde seit darf nicht in der Zukunft liegen")
        LocalDate kundeSeit,

        @Schema(example = "Weitere Telefonnummern: 0171 2556017")
        @Size(max = 500, message = "Bemerkung darf höchstens 500 Zeichen lang sein")
        String bemerkung) {

    /** Kopie mit anderer Kundennummer - der Import vergibt so Nummern fuer Altkunden ohne Nummer. */
    public KundeRequest mitKundennummer(String neueKundennummer) {
        return new KundeRequest(neueKundennummer, kundentyp, firma, anrede, vorname, nachname, email, telefon, strasse, plz,
                ort, ustId, kundeSeit, bemerkung);
    }
}
