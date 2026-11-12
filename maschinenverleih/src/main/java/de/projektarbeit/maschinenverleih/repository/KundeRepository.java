package de.projektarbeit.maschinenverleih.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import de.projektarbeit.maschinenverleih.model.Kunde;

public interface KundeRepository extends JpaRepository<Kunde, Long> {

    List<Kunde> findAllByOrderByNachnameAscVornameAsc();

    boolean existsByKundennummer(String kundennummer);

    boolean existsByEmailIgnoreCase(String email);
}
