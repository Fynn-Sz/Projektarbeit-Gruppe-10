package de.projektarbeit.maschinenverleih.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.projektarbeit.maschinenverleih.dto.KategorieRequest;
import de.projektarbeit.maschinenverleih.dto.KategorieResponse;
import de.projektarbeit.maschinenverleih.service.KategorieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/kategorien")
@Tag(name = "Kategorien", description = "Maschinenkategorien (Filter der Suche, Typ der Maschine)")
public class KategorieController {

    private final KategorieService service;

    public KategorieController(KategorieService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Alle Kategorien", description = "Fuer das Kategorie-Dropdown in Suche und Maschinenformular")
    public List<KategorieResponse> getAll() {
        return service.alle();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Kategorie anlegen")
    public KategorieResponse create(@Valid @RequestBody KategorieRequest request) {
        return service.anlegen(request);
    }
}
