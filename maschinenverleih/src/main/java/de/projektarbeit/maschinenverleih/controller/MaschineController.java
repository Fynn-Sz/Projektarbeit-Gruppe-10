package de.projektarbeit.maschinenverleih.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import de.projektarbeit.maschinenverleih.dto.ImportErgebnis;
import de.projektarbeit.maschinenverleih.dto.MaschineRequest;
import de.projektarbeit.maschinenverleih.dto.MaschineResponse;
import de.projektarbeit.maschinenverleih.service.MaschineService;
import de.projektarbeit.maschinenverleih.service.MaschinenImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/maschinen")
@Tag(name = "Maschinen", description = "Maschinenstammdaten (US 2) und Verfuegbarkeitssuche (US 1)")
public class MaschineController {

    private final MaschineService service;
    private final MaschinenImportService importService;

    public MaschineController(MaschineService service, MaschinenImportService importService) {
        this.service = service;
        this.importService = importService;
    }

    @GetMapping
    @Operation(summary = "Alle Maschinen", description = "Optional gefiltert nach Kategorie und/oder Standort")
    public List<MaschineResponse> getAll(
            @Parameter(description = "ID der Kategorie") @RequestParam(required = false) Long kategorieId,
            @Parameter(description = "ID des Standorts") @RequestParam(required = false) Long standortId) {
        return service.alle(kategorieId, standortId);
    }

    @GetMapping("/verfuegbar")
    @Operation(summary = "Verfuegbare Maschinen suchen (US 1)",
            description = "Liefert nur Maschinen mit Status VERFUEGBAR, die im Zeitraum von-bis nicht reserviert sind. "
                    + "Kategorie und Standort sind optionale Filter.")
    @ApiResponse(responseCode = "200", description = "Liste der freien Maschinen (kann leer sein)")
    @ApiResponse(responseCode = "400", description = "Zeitraum ungueltig (bis vor von) oder Datum fehlt")
    public List<MaschineResponse> search(
            @Parameter(description = "ID der Kategorie") @RequestParam(required = false) Long kategorieId,
            @Parameter(description = "ID des Standorts") @RequestParam(required = false) Long standortId,
            @Parameter(description = "Erster Miettag", example = "2026-12-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @Parameter(description = "Letzter Miettag", example = "2026-12-05")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis) {
        return service.verfuegbare(kategorieId, standortId, von, bis);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Eine Maschine")
    @ApiResponse(responseCode = "404", description = "Maschine nicht gefunden")
    public MaschineResponse getOne(@PathVariable Long id) {
        return service.finde(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Maschine anlegen")
    @ApiResponse(responseCode = "201", description = "Maschine angelegt")
    @ApiResponse(responseCode = "400", description = "Pflichtfeld fehlt oder Wert ungueltig")
    @ApiResponse(responseCode = "409", description = "Inventarnummer bereits vergeben")
    public MaschineResponse create(@Valid @RequestBody MaschineRequest request) {
        return service.anlegen(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Maschine aendern", description = "Die Aenderung wird im Audit-Trail protokolliert")
    @ApiResponse(responseCode = "404", description = "Maschine nicht gefunden")
    @ApiResponse(responseCode = "409", description = "Inventarnummer bereits vergeben")
    public MaschineResponse change(@PathVariable Long id, @Valid @RequestBody MaschineRequest request) {
        return service.aendern(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Maschine loeschen", description = "Nur moeglich, solange die Maschine keine Reservierungen hat")
    @ApiResponse(responseCode = "409", description = "Maschine hat Reservierungen")
    public void delete(@PathVariable Long id) {
        service.loeschen(id);
    }

    @GetMapping("/{id}/historie")
    @Operation(summary = "Aenderungshistorie (Audit-Trail)", description = "Neueste Eintraege zuerst")
    public List<AuditLogResponse> history(@PathVariable Long id) {
        return service.historie(id);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Maschinen aus Excel importieren",
            description = "Erwartet eine .xlsx-Datei mit Ueberschriften in Zeile 1: Inventarnummer, Bezeichnung, "
                    + "Kategorie, Standort, Baujahr, Betriebsstunden, Wartungsintervall, Tagesmietpreis "
                    + "(optional: Hersteller, Garantie bis). Vorhandene Inventarnummern werden uebersprungen, "
                    + "fehlerhafte Zeilen im Ergebnis gemeldet.")
    public ImportErgebnis importExcel(@RequestPart("datei") MultipartFile datei) {
        return importService.importiere(datei);
    }
}
