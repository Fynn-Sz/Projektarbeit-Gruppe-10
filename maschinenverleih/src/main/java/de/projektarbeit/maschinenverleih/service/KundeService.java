package de.projektarbeit.maschinenverleih.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.dto.AuditLogResponse;
import de.projektarbeit.maschinenverleih.dto.KundeRequest;
import de.projektarbeit.maschinenverleih.dto.KundeResponse;
import de.projektarbeit.maschinenverleih.model.AuditAktion;
import de.projektarbeit.maschinenverleih.model.Kunde;
import de.projektarbeit.maschinenverleih.model.Kundentyp;
import de.projektarbeit.maschinenverleih.repository.KundeRepository;

@Service
public class KundeService {

    private static final String TABELLE = "kunde";

    private final KundeRepository repository;
    private final AuditService auditService;

    public KundeService(KundeRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<KundeResponse> alle() {
        return repository.alleSortiert().stream().map(KundeResponse::von).toList();
    }

    /** Sucht in Kundennummer, Firma, Name, E-Mail, Telefon und Anschrift. Leerer Suchtext = alle Kunden. */
    @Transactional(readOnly = true)
    public List<KundeResponse> suche(String text) {
        if (text == null || text.isBlank()) {
            return alle();
        }
        String muster = "%" + text.trim().toLowerCase(Locale.GERMAN) + "%";
        return repository.suche(muster).stream().map(KundeResponse::von).toList();
    }

    @Transactional(readOnly = true)
    public KundeResponse finde(Long id) {
        return KundeResponse.von(lade(id));
    }

    @Transactional
    public KundeResponse anlegen(KundeRequest request) {
        return anlegen(request, AuditAktion.ANGELEGT);
    }

    /** @param aktion ANGELEGT (Formular/API) oder IMPORTIERT (Excel-Import) - steht so im Audit-Trail */
    @Transactional
    public KundeResponse anlegen(KundeRequest request, AuditAktion aktion) {
        String kundennummer = request.kundennummer().trim();
        String email = leerZuNull(request.email());
        if (repository.existsByKundennummer(kundennummer)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Kundennummer '" + kundennummer + "' ist bereits vergeben");
        }
        if (email != null && repository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-Mail '" + email + "' ist bereits vergeben");
        }
        var kunde = new Kunde();
        uebernehme(request, kunde);
        var neu = KundeResponse.von(repository.save(kunde));
        auditService.protokolliere(aktion, TABELLE, neu.id(), null, neu);
        return neu;
    }

    @Transactional
    public KundeResponse aendern(Long id, KundeRequest request) {
        var kunde = lade(id);
        String kundennummer = request.kundennummer().trim();
        String email = leerZuNull(request.email());
        if (repository.existsByKundennummerAndIdNot(kundennummer, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Kundennummer '" + kundennummer + "' ist bereits vergeben");
        }
        if (email != null && repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-Mail '" + email + "' ist bereits vergeben");
        }
        var alt = KundeResponse.von(kunde);
        uebernehme(request, kunde);
        var neu = KundeResponse.von(repository.save(kunde));
        auditService.protokolliere(AuditAktion.GEAENDERT, TABELLE, id, alt, neu);
        return neu;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> historie(Long id) {
        lade(id);
        return auditService.historie(TABELLE, id);
    }

    public Kunde lade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Kunde mit ID " + id + " nicht gefunden"));
    }

    private void uebernehme(KundeRequest request, Kunde kunde) {
        pruefeRegeln(request);
        kunde.setKundennummer(request.kundennummer().trim());
        kunde.setKundentyp(request.kundentyp());
        kunde.setFirma(leerZuNull(request.firma()));
        kunde.setAnrede(request.anrede());
        kunde.setVorname(leerZuNull(request.vorname()));
        kunde.setNachname(request.nachname().trim());
        kunde.setEmail(leerZuNull(request.email()));
        kunde.setTelefon(leerZuNull(request.telefon()));
        kunde.setStrasse(request.strasse().trim());
        kunde.setPlz(request.plz().trim());
        kunde.setOrt(request.ort().trim());
        kunde.setUstId(leerZuNull(request.ustId()));
        kunde.setKundeSeit(request.kundeSeit());
        kunde.setBemerkung(leerZuNull(request.bemerkung()));
    }

    /**
     * Regeln, die mehrere Felder betreffen (die einzelnen Felder prueft bereits @Valid am KundeRequest).
     * Die Meldungen beginnen mit dem Feldnamen - so kann das Webformular sie dem richtigen Feld zuordnen.
     */
    static void pruefeRegeln(KundeRequest request) {
        if (request.kundentyp() == Kundentyp.GEWERBE && leerZuNull(request.firma()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Firma ist bei Gewerbekunden ein Pflichtfeld");
        }
        if (request.kundentyp() == Kundentyp.PRIVAT && leerZuNull(request.vorname()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vorname ist bei Privatkunden ein Pflichtfeld");
        }
        if (leerZuNull(request.email()) == null && leerZuNull(request.telefon()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "E-Mail oder Telefon muss angegeben sein, damit der Kunde erreichbar ist");
        }
    }

    private static String leerZuNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
