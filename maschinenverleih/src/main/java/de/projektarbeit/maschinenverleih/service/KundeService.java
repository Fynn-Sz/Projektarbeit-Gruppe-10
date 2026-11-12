package de.projektarbeit.maschinenverleih.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.dto.KundeRequest;
import de.projektarbeit.maschinenverleih.dto.KundeResponse;
import de.projektarbeit.maschinenverleih.model.Kunde;
import de.projektarbeit.maschinenverleih.repository.KundeRepository;

@Service
public class KundeService {

    private final KundeRepository repository;

    public KundeService(KundeRepository repository) {
        this.repository = repository;
    }

    public List<KundeResponse> alle() {
        return repository.findAllByOrderByNachnameAscVornameAsc().stream().map(KundeResponse::von).toList();
    }

    public KundeResponse finde(Long id) {
        return KundeResponse.von(lade(id));
    }

    @Transactional
    public KundeResponse anlegen(KundeRequest request) {
        if (repository.existsByKundennummer(request.kundennummer())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Kundennummer '" + request.kundennummer() + "' ist bereits vergeben");
        }
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "E-Mail '" + request.email() + "' ist bereits vergeben");
        }
        var kunde = new Kunde();
        kunde.setKundennummer(request.kundennummer().trim());
        kunde.setFirma(request.firma());
        kunde.setVorname(request.vorname().trim());
        kunde.setNachname(request.nachname().trim());
        kunde.setEmail(request.email().trim());
        kunde.setTelefon(request.telefon());
        kunde.setStrasse(request.strasse().trim());
        kunde.setPlz(request.plz());
        kunde.setOrt(request.ort().trim());
        kunde.setUstId(request.ustId());
        return KundeResponse.von(repository.save(kunde));
    }

    public Kunde lade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Kunde mit ID " + id + " nicht gefunden"));
    }
}
