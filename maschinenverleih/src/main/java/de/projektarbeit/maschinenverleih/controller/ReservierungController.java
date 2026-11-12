package de.projektarbeit.maschinenverleih.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.projektarbeit.maschinenverleih.dto.ReservierungRequest;
import de.projektarbeit.maschinenverleih.dto.ReservierungResponse;
import de.projektarbeit.maschinenverleih.service.ReservierungService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservierungen")
@Tag(name = "Reservierungen", description = "Maschinen verbindlich reservieren (US 1)")
public class ReservierungController {

    private final ReservierungService service;

    public ReservierungController(ReservierungService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Reservierungen", description = "Optional nur die eines Kunden")
    public List<ReservierungResponse> getAll(
            @Parameter(description = "ID des Kunden") @RequestParam(required = false) Long kundeId) {
        return service.alle(kundeId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Eine Reservierung (z. B. fuer die Bestaetigungsseite)")
    @ApiResponse(responseCode = "404", description = "Reservierung nicht gefunden")
    public ReservierungResponse getOne(@PathVariable Long id) {
        return service.finde(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Maschine reservieren",
            description = "Speichert die Reservierung verbindlich (Status BESTAETIGT). Die Antwort dient als "
                    + "Bestaetigung: Reservierungsnummer (id), Zeitraum, Miettage und voraussichtlicher Preis.")
    @ApiResponse(responseCode = "201", description = "Reservierung gespeichert")
    @ApiResponse(responseCode = "400", description = "Eingaben ungueltig (z. B. Datum in der Vergangenheit, bis vor von)")
    @ApiResponse(responseCode = "404", description = "Kunde oder Maschine nicht gefunden")
    @ApiResponse(responseCode = "409", description = "Maschine im Zeitraum bereits reserviert oder nicht verleihbar")
    public ReservierungResponse create(@Valid @RequestBody ReservierungRequest request) {
        return service.reservieren(request);
    }

    @PostMapping("/{id}/stornieren")
    @Operation(summary = "Reservierung stornieren", description = "Die Maschine ist danach im Zeitraum wieder frei")
    @ApiResponse(responseCode = "409", description = "Reservierung ist bereits storniert")
    public ReservierungResponse cancel(@PathVariable Long id) {
        return service.stornieren(id);
    }
}
