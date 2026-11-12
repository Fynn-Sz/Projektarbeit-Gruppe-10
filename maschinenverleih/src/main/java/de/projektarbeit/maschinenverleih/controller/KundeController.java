package de.projektarbeit.maschinenverleih.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.projektarbeit.maschinenverleih.dto.KundeRequest;
import de.projektarbeit.maschinenverleih.dto.KundeResponse;
import de.projektarbeit.maschinenverleih.service.KundeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/kunden")
@Tag(name = "Kunden", description = "Kundenstammdaten (Grundlage fuer Reservierungen)")
public class KundeController {

    private final KundeService service;

    public KundeController(KundeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Alle Kunden")
    public List<KundeResponse> getAll() {
        return service.alle();
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
    @ApiResponse(responseCode = "409", description = "Kundennummer oder E-Mail bereits vergeben")
    public KundeResponse create(@Valid @RequestBody KundeRequest request) {
        return service.anlegen(request);
    }
}
