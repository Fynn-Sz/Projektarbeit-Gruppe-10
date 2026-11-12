package de.projektarbeit.maschinenverleih.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import de.projektarbeit.maschinenverleih.model.Standort;

public interface StandortRepository extends JpaRepository<Standort, Long> {

    List<Standort> findAllByOrderByNameAsc();

    Optional<Standort> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
