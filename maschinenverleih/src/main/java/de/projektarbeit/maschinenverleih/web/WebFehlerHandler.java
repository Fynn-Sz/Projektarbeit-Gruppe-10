package de.projektarbeit.maschinenverleih.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Fehler auf den Webseiten (z. B. Maschine nicht gefunden) als HTML-Fehlerseite anzeigen
 * statt als JSON wie bei der REST-API.
 */
@ControllerAdvice(basePackages = "de.projektarbeit.maschinenverleih.web")
public class WebFehlerHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView fehler(ResponseStatusException ex) {
        var seite = new ModelAndView("error");
        seite.setStatus(ex.getStatusCode());
        seite.addObject("status", ex.getStatusCode().value());
        seite.addObject("grund", ex.getReason());
        return seite;
    }
}
