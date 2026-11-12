package de.projektarbeit.maschinenverleih.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.projektarbeit.maschinenverleih.dto.StandortRequest;
import de.projektarbeit.maschinenverleih.dto.StandortResponse;
import de.projektarbeit.maschinenverleih.service.StandortService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/standorte")
@Tag(name = "Standorte", description = "Standorte, an denen Maschinen abgeholt werden")
public class StandortController {

    private final StandortService service;

    public StandortController(StandortService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Alle Standorte", description = "Fuer das Standort-Dropdown in Suche und Maschinenformular")
    public List<StandortResponse> getAll() {
        return service.alle();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Standort anlegen")
    public StandortResponse create(@Valid @RequestBody StandortRequest request) {
        return service.anlegen(request);
    }
}
