package de.projektarbeit.maschinenverleih.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.projektarbeit.maschinenverleih.dto.AuditLogResponse;
import de.projektarbeit.maschinenverleih.dto.KundeRequest;
import de.projektarbeit.maschinenverleih.dto.KundeResponse;
import de.projektarbeit.maschinenverleih.dto.KundenImportErgebnis;
import de.projektarbeit.maschinenverleih.service.KundeService;
import de.projektarbeit.maschinenverleih.service.KundenImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/kunden")
@Tag(name = "Kunden", description = "Kundenstammdaten (Grundlage für Reservierungen) und Übernahme der Altdaten")
public class KundeController {

    private final KundeService service;
    private final KundenImportService importService;

    public KundeController(KundeService service, KundenImportService importService) {
        this.service = service;
        this.importService = importService;
    }

    @GetMapping
    @Operation(summary = "Alle Kunden oder Kunden suchen",
            description = "Ohne Suchtext alle Kunden. Mit Suchtext: Treffer in Kundennummer, Firma, Name, E-Mail, "
                    + "Telefon oder Anschrift (Groß-/Kleinschreibung egal).")
    public List<KundeResponse> getAll(
            @Parameter(description = "Suchtext, z. B. \"Krause\", \"K-1003\" oder \"42853\"")
            @RequestParam(required = false) String suche) {
        return service.suche(suche);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ein Kunde")
    @ApiResponse(responseCode = "404", description = "Kunde nicht gefunden")
    public KundeResponse getOne(@PathVariable Long id) {
        return service.finde(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Kunde anlegen")
    @ApiResponse(responseCode = "400", description = "Pflichtfeld fehlt oder Wert ungültig "
            + "(z. B. Gewerbekunde ohne Firma, weder E-Mail noch Telefon)")
    @ApiResponse(responseCode = "409", description = "Kundennummer oder E-Mail bereits vergeben")
    public KundeResponse create(@Valid @RequestBody KundeRequest request) {
        return service.anlegen(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Kunde ändern", description = "Die Änderung wird im Audit-Trail protokolliert")
    @ApiResponse(responseCode = "404", description = "Kunde nicht gefunden")
    @ApiResponse(responseCode = "409", description = "Kundennummer oder E-Mail bereits vergeben")
    public KundeResponse change(@PathVariable Long id, @Valid @RequestBody KundeRequest request) {
        return service.aendern(id, request);
    }

    @GetMapping("/{id}/historie")
    @Operation(summary = "Änderungshistorie eines Kunden (Audit-Trail)", description = "Neueste Einträge zuerst")
    public List<AuditLogResponse> history(@PathVariable Long id) {
        return service.historie(id);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Kunden aus dem alten Excel-System übernehmen",
            description = "Erwartet die Altdaten-Datei (.xlsx). Gesucht wird das Tabellenblatt mit den Spalten "
                    + "KdNr, Kunde / Ansprechpartner und Adresse (optional Telefon, E-Mail, Kundentyp, Kunde seit, "
                    + "Anmerkung). Fehlerhafte Zeilen und Dubletten werden nicht übernommen, sondern im Ergebnis "
                    + "gemeldet. Der Import ist wiederholbar: Vorhandene Kunden werden nicht doppelt angelegt.")
    @ApiResponse(responseCode = "200", description = "Importprotokoll (angelegt, vorhanden, Dubletten, Fehler, Hinweise)")
    @ApiResponse(responseCode = "400", description = "Keine/ungültige Datei oder kein Kunden-Tabellenblatt gefunden")
    public KundenImportErgebnis importExcel(@RequestPart("datei") MultipartFile datei) {
        return importService.importiere(datei);
    }
}
