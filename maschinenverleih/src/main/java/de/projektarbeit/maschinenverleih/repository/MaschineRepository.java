package de.projektarbeit.maschinenverleih.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import de.projektarbeit.maschinenverleih.model.Maschine;
public interface MaschineRepository extends JpaRepository<Maschine, Long> {

    List<Maschine> findByTypIgnoreCase(String typ);

    boolean existsByBezeichnung(String bezeichnung);
}
