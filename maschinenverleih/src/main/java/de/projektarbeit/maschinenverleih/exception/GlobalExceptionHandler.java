package de.projektarbeit.maschinenverleih.exception;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Wandelt Fehler in einheitliche Antworten im Format "Problem Details" (RFC 9457) um.
 * ResponseStatusException aus den Services (404, 409, ...) behandelt die Basisklasse bereits.
 * Gilt nur fuer die REST-Controller - die Webseiten haben einen eigenen Handler (web.WebFehlerHandler).
 */
@RestControllerAdvice(basePackages = "de.projektarbeit.maschinenverleih.controller")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 400 bei @Valid-Fehlern: zusaetzlich jede fehlerhafte Eingabe mit ihrer Meldung
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> fehler = new TreeMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fehler.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Eingaben sind ungültig");
        problem.setProperty("fehler", fehler);
        return ResponseEntity.badRequest().body(problem);
    }

    // Letzte Sicherung, falls die Datenbank eine Regel (UNIQUE, CHECK, Fremdschluessel) verletzt sieht
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail datenbankRegelVerletzt(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Die Daten verletzen eine Regel der Datenbank (z. B. doppelter Wert oder noch verwendeter Datensatz)");
    }
}
