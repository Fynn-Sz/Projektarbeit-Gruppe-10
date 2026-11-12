package de.projektarbeit.maschinenverleih.dto;

import java.time.LocalDateTime;

import de.projektarbeit.maschinenverleih.model.AuditAktion;
import de.projektarbeit.maschinenverleih.model.AuditLog;

public record AuditLogResponse(
        LocalDateTime zeitpunkt,
        String benutzer,
        AuditAktion aktion,
        String alteWerte,
        String neueWerte) {

    public static AuditLogResponse von(AuditLog log) {
        return new AuditLogResponse(log.getZeitpunkt(), log.getBenutzer(), log.getAktion(),
                log.getAlteWerte(), log.getNeueWerte());
    }
}
