package de.projektarbeit.maschinenverleih.web;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Bruecke zwischen den Pruefregeln der Request-Records und den Fehlermeldungen im HTML-Formular.
 */
@Component
public class Formularpruefung {

    private final Validator validator;

    public Formularpruefung(Validator validator) {
        this.validator = validator;
    }

    /** Prueft den Request und haengt jeden Fehler an das gleichnamige Formularfeld. */
    public void pruefe(Object request, BindingResult ergebnis) {
        for (var verstoss : validator.validate(request)) {
            String feld = verstoss.getPropertyPath().toString();
            // Feld hat schon einen Fehler (z. B. Text statt Zahl) - nicht doppelt melden
            if (ergebnis.getFieldErrorCount(feld) == 0) {
                ergebnis.rejectValue(feld, "ungueltig", verstoss.getMessage());
            }
        }
    }

    /** Nur die Meldungen - fuer Formulare ohne Formularobjekt (z. B. Reservierung). */
    public List<String> meldungen(Object request) {
        return validator.validate(request).stream().map(ConstraintViolation::getMessage).sorted().toList();
    }

    /**
     * Fachliche Fehler aus dem Service (z. B. 409 "Inventarnummer bereits vergeben") anzeigen:
     * direkt am passenden Feld, sonst als allgemeine Meldung oben im Formular.
     *
     * @param felder Anfang der Meldung -> Formularfeld, z. B. "Inventarnummer" -> "inventarnummer"
     */
    public void uebernehme(ResponseStatusException fehler, BindingResult ergebnis, Map<String, String> felder) {
        String meldung = fehler.getReason();
        for (var eintrag : felder.entrySet()) {
            if (meldung != null && meldung.startsWith(eintrag.getKey())) {
                ergebnis.rejectValue(eintrag.getValue(), "fachlich", meldung);
                return;
            }
        }
        ergebnis.reject("fachlich", meldung);
    }
}
