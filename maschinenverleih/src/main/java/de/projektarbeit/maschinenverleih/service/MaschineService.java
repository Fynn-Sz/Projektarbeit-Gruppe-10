package de.projektarbeit.maschinenverleih.service;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.dto.AuditLogResponse;
import de.projektarbeit.maschinenverleih.dto.MaschineRequest;
import de.projektarbeit.maschinenverleih.dto.MaschineResponse;
import de.projektarbeit.maschinenverleih.model.AuditAktion;
import de.projektarbeit.maschinenverleih.model.Maschine;
import de.projektarbeit.maschinenverleih.model.MaschinenStatus;
import de.projektarbeit.maschinenverleih.repository.MaschineRepository;
import de.projektarbeit.maschinenverleih.repository.ReservierungRepository;

@Service
public class MaschineService {

    private static final String TABELLE = "maschine";

    private final MaschineRepository repository;
    private final ReservierungRepository reservierungRepository;
    private final KategorieService kategorieService;
    private final StandortService standortService;
    private final AuditService auditService;

    public MaschineService(MaschineRepository repository, ReservierungRepository reservierungRepository,
            KategorieService kategorieService, StandortService standortService, AuditService auditService) {
        this.repository = repository;
        this.reservierungRepository = reservierungRepository;
        this.kategorieService = kategorieService;
        this.standortService = standortService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<MaschineResponse> alle(Long kategorieId, Long standortId) {
        return repository.suche(kategorieId, standortId).stream().map(MaschineResponse::von).toList();
    }

    @Transactional(readOnly = true)
    public MaschineResponse finde(Long id) {
        return MaschineResponse.von(lade(id));
    }

    @Transactional(readOnly = true)
    public List<MaschineResponse> verfuegbare(Long kategorieId, Long standortId, LocalDate von, LocalDate bis) {
        pruefeZeitraum(von, bis);
        return repository.sucheVerfuegbare(kategorieId, standortId, von, bis).stream()
                .map(MaschineResponse::von)
                .toList();
    }

    @Transactional
    public MaschineResponse anlegen(MaschineRequest request) {
        return anlegen(request, AuditAktion.ANGELEGT);
    }

    @Transactional
    public MaschineResponse anlegen(MaschineRequest request, AuditAktion aktion) {
        if (repository.existsByInventarnummer(request.inventarnummer().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inventarnummer '" + request.inventarnummer() + "' ist bereits vergeben");
        }
        var maschine = new Maschine();
        uebernehme(request, maschine);
        var neu = MaschineResponse.von(repository.save(maschine));
        auditService.protokolliere(aktion, TABELLE, neu.id(), null, neu);
        return neu;
    }

    @Transactional
    public MaschineResponse aendern(Long id, MaschineRequest request) {
        var maschine = lade(id);
        if (repository.existsByInventarnummerAndIdNot(request.inventarnummer().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inventarnummer '" + request.inventarnummer() + "' ist bereits vergeben");
        }
        var alt = MaschineResponse.von(maschine);
        uebernehme(request, maschine);
        var neu = MaschineResponse.von(repository.save(maschine));
        auditService.protokolliere(AuditAktion.GEAENDERT, TABELLE, id, alt, neu);
        return neu;
    }

    @Transactional
    public void loeschen(Long id) {
        var maschine = lade(id);
        if (reservierungRepository.existsByMaschineId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Maschine hat Reservierungen und kann nicht gelöscht werden – bitte stattdessen den Status „Außer Betrieb“ setzen");
        }
        var alt = MaschineResponse.von(maschine);
        repository.delete(maschine);
        auditService.protokolliere(AuditAktion.GELOESCHT, TABELLE, id, alt, null);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> historie(Long id) {
        return auditService.historie(TABELLE, id);
    }

    public boolean existiert(String inventarnummer) {
        return repository.existsByInventarnummer(inventarnummer);
    }

    private Maschine lade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Maschine mit ID " + id + " nicht gefunden"));
    }

    private void uebernehme(MaschineRequest request, Maschine maschine) {
        if (request.baujahr() > Year.now().getValue()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Baujahr darf nicht in der Zukunft liegen");
        }
        maschine.setInventarnummer(request.inventarnummer().trim());
        maschine.setBezeichnung(request.bezeichnung().trim());
        maschine.setKategorie(kategorieService.lade(request.kategorieId()));
        maschine.setStandort(standortService.lade(request.standortId()));
        maschine.setHersteller(request.hersteller());
        maschine.setBaujahr(request.baujahr());
        maschine.setBetriebsstunden(request.betriebsstunden());
        maschine.setWartungsintervallStd(request.wartungsintervallStd());
        maschine.setTagesmietpreis(request.tagesmietpreis());
        maschine.setGarantieBis(request.garantieBis());
        maschine.setStatus(request.status() == null ? MaschinenStatus.VERFUEGBAR : request.status());
    }

    static void pruefeZeitraum(LocalDate von, LocalDate bis) {
        if (bis.isBefore(von)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enddatum darf nicht vor dem Startdatum liegen");
        }
    }
}
