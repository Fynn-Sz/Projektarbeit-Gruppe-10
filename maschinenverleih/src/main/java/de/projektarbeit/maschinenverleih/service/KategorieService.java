package de.projektarbeit.maschinenverleih.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.dto.KategorieRequest;
import de.projektarbeit.maschinenverleih.dto.KategorieResponse;
import de.projektarbeit.maschinenverleih.model.Kategorie;
import de.projektarbeit.maschinenverleih.repository.KategorieRepository;

@Service
public class KategorieService {

    private final KategorieRepository repository;

    public KategorieService(KategorieRepository repository) {
        this.repository = repository;
    }

    public List<KategorieResponse> alle() {
        return repository.findAllByOrderByNameAsc().stream().map(KategorieResponse::von).toList();
    }

    @Transactional
    public KategorieResponse anlegen(KategorieRequest request) {
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Kategorie '" + name + "' existiert bereits");
        }
        return KategorieResponse.von(repository.save(new Kategorie(name)));
    }

    public Kategorie lade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Kategorie mit ID " + id + " nicht gefunden"));
    }
}
