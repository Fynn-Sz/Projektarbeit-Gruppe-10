package de.projektarbeit.maschinenverleih.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.projektarbeit.maschinenverleih.model.Kunde;

public interface KundeRepository extends JpaRepository<Kunde, Long> {

    /** Alphabetisch nach dem, was in der Liste vorne steht: Firmenname, sonst Nachname. */
    @Query("select k from Kunde k order by lower(coalesce(k.firma, k.nachname)), lower(k.nachname), lower(k.vorname)")
    List<Kunde> alleSortiert();

    /**
     * Suche in den wichtigsten Feldern, Gross-/Kleinschreibung egal.
     * @param muster z. B. "%krause%" - der Service setzt die Platzhalter
     */
    @Query("""
            select k from Kunde k
            where lower(k.kundennummer) like :muster
               or lower(coalesce(k.firma, '')) like :muster
               or lower(coalesce(k.vorname, '')) like :muster
               or lower(k.nachname) like :muster
               or lower(coalesce(k.email, '')) like :muster
               or lower(coalesce(k.telefon, '')) like :muster
               or lower(k.strasse) like :muster
               or k.plz like :muster
               or lower(k.ort) like :muster
            order by lower(coalesce(k.firma, k.nachname)), lower(k.nachname), lower(k.vorname)""")
    List<Kunde> suche(@Param("muster") String muster);

    boolean existsByKundennummer(String kundennummer);

    boolean existsByKundennummerAndIdNot(String kundennummer, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
