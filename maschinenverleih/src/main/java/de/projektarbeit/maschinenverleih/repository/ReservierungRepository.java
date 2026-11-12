package de.projektarbeit.maschinenverleih.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.projektarbeit.maschinenverleih.model.Reservierung;

public interface ReservierungRepository extends JpaRepository<Reservierung, Long> {

    @Query("""
            SELECT COUNT(r) > 0 FROM Reservierung r
            WHERE r.maschine.id = :maschineId
              AND r.status <> de.projektarbeit.maschinenverleih.model.ReservierungsStatus.STORNIERT
              AND r.von <= :bis AND r.bis >= :von
            """)
    boolean istBelegt(Long maschineId, LocalDate von, LocalDate bis);

    @Query("""
            SELECT r FROM Reservierung r
            JOIN FETCH r.kunde JOIN FETCH r.maschine m JOIN FETCH m.standort
            WHERE (:kundeId IS NULL OR r.kunde.id = :kundeId)
            ORDER BY r.von DESC
            """)
    List<Reservierung> suche(Long kundeId);

    boolean existsByMaschineId(Long maschineId);
}
