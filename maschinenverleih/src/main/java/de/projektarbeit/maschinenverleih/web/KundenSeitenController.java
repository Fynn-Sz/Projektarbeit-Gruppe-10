package de.projektarbeit.maschinenverleih.web;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.service.KundeService;

/**
 * Kunden anzeigen und anlegen - wird fuer die Reservierung gebraucht, bis es einen Login gibt (US 8).
 * Kommt man aus der Reservierung, geht es nach dem Speichern direkt dorthin zurueck.
 */
@Controller
@RequestMapping("/kunden")
public class KundenSeitenController {

    private static final Map<String, String> FELDER = Map.of(
            "Kundennummer", "kundennummer",
            "E-Mail", "email");

    private final KundeService service;
    private final Formularpruefung pruefung;

    public KundenSeitenController(KundeService service, Formularpruefung pruefung) {
        this.service = service;
        this.pruefung = pruefung;
    }

    @ModelAttribute("seite")
    public String seite() {
        return "kunden";
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("kunden", service.alle());
        return "kunden/liste";
    }

    @GetMapping("/neu")
    public String neu(@RequestParam(required = false) Long maschineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            Model model) {
        model.addAttribute("formular", new KundeFormular());
        merkeRueckweg(model, maschineId, von, bis);
        return "kunden/formular";
    }

    @PostMapping
    public String anlegen(@ModelAttribute("formular") KundeFormular formular, BindingResult ergebnis,
            @RequestParam(required = false) Long maschineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            Model model, RedirectAttributes weiterleitung) {
        pruefung.pruefe(formular.alsRequest(), ergebnis);
        if (!ergebnis.hasErrors()) {
            try {
                var neu = service.anlegen(formular.alsRequest());
                if (maschineId != null && von != null && bis != null) {
                    // zurueck zur Reservierung, der neue Kunde ist dort schon ausgewaehlt
                    weiterleitung.addAttribute("maschineId", maschineId);
                    // als ISO-Text (2026-12-01), sonst formatiert Spring das Datum deutsch und die Adresse passt nicht
                    weiterleitung.addAttribute("von", von.toString());
                    weiterleitung.addAttribute("bis", bis.toString());
                    weiterleitung.addAttribute("kundeId", neu.id());
                    weiterleitung.addFlashAttribute("meldung", "Kunde " + neu.kundennummer() + " wurde angelegt.");
                    return "redirect:/reservieren";
                }
                weiterleitung.addFlashAttribute("meldung",
                        "Kunde " + neu.kundennummer() + " (" + neu.vorname() + " " + neu.nachname() + ") wurde angelegt.");
                return "redirect:/kunden";
            } catch (ResponseStatusException e) {
                pruefung.uebernehme(e, ergebnis, FELDER);
            }
        }
        merkeRueckweg(model, maschineId, von, bis);
        return "kunden/formular";
    }

    private void merkeRueckweg(Model model, Long maschineId, LocalDate von, LocalDate bis) {
        model.addAttribute("maschineId", maschineId);
        model.addAttribute("von", von);
        model.addAttribute("bis", bis);
    }
}
