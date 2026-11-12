package de.projektarbeit.maschinenverleih.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.dto.StandortRequest;
import de.projektarbeit.maschinenverleih.dto.StandortResponse;
import de.projektarbeit.maschinenverleih.model.Standort;
import de.projektarbeit.maschinenverleih.repository.StandortRepository;

@Service
public class StandortService {

    private final StandortRepository repository;

    public StandortService(StandortRepository repository) {
        this.repository = repository;
    }

    public List<StandortResponse> alle() {
        return repository.findAllByOrderByNameAsc().stream().map(StandortResponse::von).toList();
    }

    @Transactional
    public StandortResponse anlegen(StandortRequest request) {
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Standort '" + name + "' existiert bereits");
        }
        var standort = new Standort(name, request.strasse(), request.plz(), request.ort());
        return StandortResponse.von(repository.save(standort));
    }

    public Standort lade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Standort mit ID " + id + " nicht gefunden"));
    }
}
