package de.projektarbeit.maschinenverleih.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import de.projektarbeit.maschinenverleih.model.Kategorie;

public interface KategorieRepository extends JpaRepository<Kategorie, Long> {

    List<Kategorie> findAllByOrderByNameAsc();

    Optional<Kategorie> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
