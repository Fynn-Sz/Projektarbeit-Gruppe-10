package de.projektarbeit.maschinenverleih.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.projektarbeit.maschinenverleih.dto.AuditLogResponse;
import de.projektarbeit.maschinenverleih.model.AuditAktion;
import de.projektarbeit.maschinenverleih.model.AuditLog;
import de.projektarbeit.maschinenverleih.repository.AuditLogRepository;

@Service
public class AuditService {

    // Platzhalter, bis es einen Login gibt (US 8) - dann steht hier der angemeldete Benutzer
    private static final String BENUTZER = "system";

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void protokolliere(AuditAktion aktion, String tabelle, Long datensatzId, Object alt, Object neu) {
        repository.save(new AuditLog(BENUTZER, aktion, tabelle, datensatzId,
                alt == null ? null : alt.toString(),
                neu == null ? null : neu.toString()));
    }

    public List<AuditLogResponse> historie(String tabelle, Long datensatzId) {
        return repository.findByTabelleAndDatensatzIdOrderByZeitpunktDesc(tabelle, datensatzId).stream()
                .map(AuditLogResponse::von)
                .toList();
    }
}
