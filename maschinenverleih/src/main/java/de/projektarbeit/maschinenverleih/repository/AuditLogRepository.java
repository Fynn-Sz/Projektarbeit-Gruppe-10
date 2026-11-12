package de.projektarbeit.maschinenverleih.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import de.projektarbeit.maschinenverleih.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTabelleAndDatensatzIdOrderByZeitpunktDesc(String tabelle, Long datensatzId);
}
