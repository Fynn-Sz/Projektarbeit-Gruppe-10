package de.projektarbeit.maschinenverleih.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import de.projektarbeit.maschinenverleih.model.Maschine;
import jakarta.persistence.LockModeType;

public interface MaschineRepository extends JpaRepository<Maschine, Long> {

    // JOIN FETCH laedt Kategorie und Standort in derselben Abfrage mit (statt einer Abfrage pro Maschine)
    @Query("""
            SELECT m FROM Maschine m
            JOIN FETCH m.kategorie JOIN FETCH m.standort
            WHERE (:kategorieId IS NULL OR m.kategorie.id = :kategorieId)
              AND (:standortId IS NULL OR m.standort.id = :standortId)
            ORDER BY m.bezeichnung
            """)
    List<Maschine> suche(Long kategorieId, Long standortId);

    /**
     * Verfuegbar = Status VERFUEGBAR und keine aktive Reservierung, die sich mit dem Zeitraum ueberschneidet.
     * Zwei Zeitraeume ueberschneiden sich, wenn: vorhandene.von <= gewuenscht.bis UND vorhandene.bis >= gewuenscht.von
     */
    @Query("""
            SELECT m FROM Maschine m
            JOIN FETCH m.kategorie JOIN FETCH m.standort
            WHERE m.status = de.projektarbeit.maschinenverleih.model.MaschinenStatus.VERFUEGBAR
              AND (:kategorieId IS NULL OR m.kategorie.id = :kategorieId)
              AND (:standortId IS NULL OR m.standort.id = :standortId)
              AND NOT EXISTS (
                  SELECT r FROM Reservierung r
                  WHERE r.maschine = m
                    AND r.status <> de.projektarbeit.maschinenverleih.model.ReservierungsStatus.STORNIERT
                    AND r.von <= :bis AND r.bis >= :von)
            ORDER BY m.bezeichnung
            """)
    List<Maschine> sucheVerfuegbare(Long kategorieId, Long standortId, LocalDate von, LocalDate bis);

    // sperrt die Zeile (SELECT ... FOR UPDATE), bis die Transaktion endet:
    // zwei gleichzeitige Reservierungen derselben Maschine laufen dadurch nacheinander
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Maschine m WHERE m.id = :id")
    Optional<Maschine> findeUndSperre(Long id);

    boolean existsByInventarnummer(String inventarnummer);

    boolean existsByInventarnummerAndIdNot(String inventarnummer, Long id);
}
