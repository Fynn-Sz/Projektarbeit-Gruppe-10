package de.projektarbeit.maschinenverleih.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.dto.ReservierungRequest;
import de.projektarbeit.maschinenverleih.dto.ReservierungResponse;
import de.projektarbeit.maschinenverleih.model.AuditAktion;
import de.projektarbeit.maschinenverleih.model.MaschinenStatus;
import de.projektarbeit.maschinenverleih.model.Reservierung;
import de.projektarbeit.maschinenverleih.model.ReservierungsStatus;
import de.projektarbeit.maschinenverleih.repository.MaschineRepository;
import de.projektarbeit.maschinenverleih.repository.ReservierungRepository;

@Service
public class ReservierungService {

    private static final String TABELLE = "reservierung";

    private final ReservierungRepository repository;
    private final MaschineRepository maschineRepository;
    private final KundeService kundeService;
    private final AuditService auditService;

    public ReservierungService(ReservierungRepository repository, MaschineRepository maschineRepository,
            KundeService kundeService, AuditService auditService) {
        this.repository = repository;
        this.maschineRepository = maschineRepository;
        this.kundeService = kundeService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ReservierungResponse> alle(Long kundeId) {
        return repository.suche(kundeId).stream().map(ReservierungResponse::von).toList();
    }

    @Transactional(readOnly = true)
    public ReservierungResponse finde(Long id) {
        return ReservierungResponse.von(lade(id));
    }

    /**
     * Laut US 1 ist die Reservierung verbindlich - sie wird deshalb direkt mit Status BESTAETIGT gespeichert.
     */
    @Transactional
    public ReservierungResponse reservieren(ReservierungRequest request) {
        MaschineService.pruefeZeitraum(request.von(), request.bis());
        var kunde = kundeService.lade(request.kundeId());
        var maschine = maschineRepository.findeUndSperre(request.maschineId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Maschine mit ID " + request.maschineId() + " nicht gefunden"));

        if (maschine.getStatus() != MaschinenStatus.VERFUEGBAR) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Maschine kann derzeit nicht reserviert werden (Status " + maschine.getStatus() + ")");
        }
        if (repository.istBelegt(maschine.getId(), request.von(), request.bis())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Maschine ist im gewählten Zeitraum bereits reserviert");
        }

        var reservierung = repository.save(new Reservierung(kunde, maschine, request.von(), request.bis(),
                ReservierungsStatus.BESTAETIGT));
        var neu = ReservierungResponse.von(reservierung);
        auditService.protokolliere(AuditAktion.ANGELEGT, TABELLE, neu.id(), null, neu);
        return neu;
    }

    @Transactional
    public ReservierungResponse stornieren(Long id) {
        var reservierung = lade(id);
        if (reservierung.getStatus() == ReservierungsStatus.STORNIERT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservierung ist bereits storniert");
        }
        var alt = ReservierungResponse.von(reservierung);
        reservierung.setStatus(ReservierungsStatus.STORNIERT);
        var neu = ReservierungResponse.von(reservierung);
        auditService.protokolliere(AuditAktion.GEAENDERT, TABELLE, id, alt, neu);
        return neu;
    }

    private Reservierung lade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reservierung mit ID " + id + " nicht gefunden"));
    }
}
