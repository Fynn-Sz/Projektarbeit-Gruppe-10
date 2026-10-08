package de.projektarbeit.maschinenverleih.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.model.Maschine;
import de.projektarbeit.maschinenverleih.repository.MaschineRepository;

@Service
public class MaschineService {

    private final MaschineRepository repository;

    public MaschineService(MaschineRepository repository) {
        this.repository = repository;
    }

    public List<Maschine> alle(String typ) {
        if (typ == null || typ.isBlank()) {
            return repository.findAll();
        }
        return repository.findByTypIgnoreCase(typ);
    }

    public Maschine finde(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Maschine mit ID " + id + " nicht gefunden"));
    }

    public Maschine anlegen(Maschine maschine) {
        if (repository.existsByBezeichnung(maschine.getBezeichnung())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Maschine '" + maschine.getBezeichnung() + "' existiert bereits");
        }
        maschine.setId(null); // ID vergibt die Datenbank
        return repository.save(maschine);
    }

    public Maschine aendern(Long id, Maschine neueDaten) {
        var maschine = finde(id);
        maschine.setBezeichnung(neueDaten.getBezeichnung());
        maschine.setTyp(neueDaten.getTyp());
        maschine.setBaujahr(neueDaten.getBaujahr());
        maschine.setBetriebsstunden(neueDaten.getBetriebsstunden());
        maschine.setWartungsintervall(neueDaten.getWartungsintervall());
        maschine.setStandort(neueDaten.getStandort());
        return repository.save(maschine);
    }

    public void loeschen(Long id) {
        repository.delete(finde(id));
    }
}
