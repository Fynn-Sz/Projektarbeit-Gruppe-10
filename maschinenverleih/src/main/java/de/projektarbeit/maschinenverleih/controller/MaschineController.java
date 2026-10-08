package de.projektarbeit.maschinenverleih.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.projektarbeit.maschinenverleih.model.Maschine;
import de.projektarbeit.maschinenverleih.service.MaschineService;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/maschinen")
public class MaschineController {

    private final MaschineService service;

    public MaschineController(MaschineService service) {
        this.service = service;
    }

    @GetMapping
    public List<Maschine> GetAll(@RequestParam(required = false) String typ) {
        return service.alle(typ);
    }

    @GetMapping("/{id}")
    public Maschine GetOne(@PathVariable Long id) {
        return service.finde(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Maschine Create(@Valid @RequestBody Maschine maschine) {
        return service.anlegen(maschine);
    }

    @PutMapping("/{id}")
    public Maschine Change(@PathVariable Long id, @Valid @RequestBody Maschine maschine) {
        return service.aendern(id, maschine);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void Delete(@PathVariable Long id) {
        service.loeschen(id);
    }
}
