package de.projektarbeit.maschinenverleih.web;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.model.Anrede;
import de.projektarbeit.maschinenverleih.model.Kundentyp;
import de.projektarbeit.maschinenverleih.service.KundeService;
import de.projektarbeit.maschinenverleih.service.KundenImportService;

/**
 * Kunden anzeigen, suchen, anlegen, bearbeiten und aus den Altdaten importieren.
 * Kommt man aus der Reservierung, geht es nach dem Anlegen direkt dorthin zurueck.
 */
@Controller
@RequestMapping("/kunden")
public class KundenSeitenController {

    // Welche Service-Meldung zu welchem Formularfeld gehoert
    private static final Map<String, String> FELDER = Map.of(
            "Kundennummer", "kundennummer",
            "E-Mail", "email",
            "Firma", "firma",
            "Vorname", "vorname");

    private final KundeService service;
    private final KundenImportService importService;
    private final Formularpruefung pruefung;

    public KundenSeitenController(KundeService service, KundenImportService importService, Formularpruefung pruefung) {
        this.service = service;
        this.importService = importService;
        this.pruefung = pruefung;
    }

    @ModelAttribute("seite")
    public String seite() {
        return "kunden";
    }

    @GetMapping
    public String liste(@RequestParam(required = false) String suche, Model model) {
        model.addAttribute("kunden", service.suche(suche));
        model.addAttribute("suche", suche == null ? "" : suche.trim());
        return "kunden/liste";
    }

    @GetMapping("/neu")
    public String neu(@RequestParam(required = false) Long maschineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            Model model) {
        merkeRueckweg(model, maschineId, von, bis);
        return formular(model, new KundeFormular(), null);
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
                        "Kunde " + neu.kundennummer() + " (" + neu.anzeigename() + ") wurde angelegt.");
                return "redirect:/kunden";
            } catch (ResponseStatusException e) {
                pruefung.uebernehme(e, ergebnis, FELDER);
            }
        }
        merkeRueckweg(model, maschineId, von, bis);
        return formular(model, formular, null);
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeiten(@PathVariable Long id, Model model) {
        return formular(model, KundeFormular.von(service.finde(id)), id);
    }

    @PostMapping("/{id}")
    public String aendern(@PathVariable Long id, @ModelAttribute("formular") KundeFormular formular,
            BindingResult ergebnis, Model model, RedirectAttributes weiterleitung) {
        pruefung.pruefe(formular.alsRequest(), ergebnis);
        if (!ergebnis.hasErrors()) {
            try {
                var neu = service.aendern(id, formular.alsRequest());
                weiterleitung.addFlashAttribute("meldung",
                        "Änderungen an " + neu.kundennummer() + " wurden gespeichert und protokolliert.");
                return "redirect:/kunden";
            } catch (ResponseStatusException e) {
                pruefung.uebernehme(e, ergebnis, FELDER);
            }
        }
        return formular(model, formular, id);
    }

    @GetMapping("/import")
    public String importSeite() {
        return "kunden/import";
    }

    @PostMapping("/import")
    public String importieren(@RequestParam(value = "datei", required = false) MultipartFile datei, Model model) {
        try {
            model.addAttribute("ergebnis", importService.importiere(datei));
            model.addAttribute("dateiname", datei.getOriginalFilename());
        } catch (ResponseStatusException e) {
            model.addAttribute("fehler", e.getReason());
        }
        return "kunden/import";
    }

    private String formular(Model model, KundeFormular formular, Long id) {
        model.addAttribute("formular", formular);
        model.addAttribute("kundeId", id);
        model.addAttribute("kundentypen", Kundentyp.values());
        model.addAttribute("anreden", Anrede.values());
        return "kunden/formular";
    }

    private void merkeRueckweg(Model model, Long maschineId, LocalDate von, LocalDate bis) {
        model.addAttribute("maschineId", maschineId);
        model.addAttribute("von", von);
        model.addAttribute("bis", bis);
    }
}
