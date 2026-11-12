package de.projektarbeit.maschinenverleih.web;

import java.util.Map;

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

import de.projektarbeit.maschinenverleih.model.MaschinenStatus;
import de.projektarbeit.maschinenverleih.service.KategorieService;
import de.projektarbeit.maschinenverleih.service.MaschineService;
import de.projektarbeit.maschinenverleih.service.MaschinenImportService;
import de.projektarbeit.maschinenverleih.service.StandortService;

/**
 * US 2: Maschinenstammdaten anzeigen, anlegen, bearbeiten, loeschen, importieren und Historie ansehen.
 */
@Controller
@RequestMapping("/maschinen")
public class MaschinenSeitenController {

    // Welche Service-Meldung zu welchem Formularfeld gehoert
    private static final Map<String, String> FELDER = Map.of(
            "Inventarnummer", "inventarnummer",
            "Baujahr", "baujahr",
            "Kategorie", "kategorieId",
            "Standort", "standortId");

    private final MaschineService service;
    private final MaschinenImportService importService;
    private final KategorieService kategorieService;
    private final StandortService standortService;
    private final Formularpruefung pruefung;

    public MaschinenSeitenController(MaschineService service, MaschinenImportService importService,
            KategorieService kategorieService, StandortService standortService, Formularpruefung pruefung) {
        this.service = service;
        this.importService = importService;
        this.kategorieService = kategorieService;
        this.standortService = standortService;
        this.pruefung = pruefung;
    }

    @ModelAttribute("seite")
    public String seite() {
        return "maschinen";
    }

    @GetMapping
    public String liste(@RequestParam(required = false) Long kategorieId,
            @RequestParam(required = false) Long standortId, Model model) {
        model.addAttribute("maschinen", service.alle(kategorieId, standortId));
        model.addAttribute("kategorien", kategorieService.alle());
        model.addAttribute("standorte", standortService.alle());
        model.addAttribute("kategorieId", kategorieId);
        model.addAttribute("standortId", standortId);
        return "maschinen/liste";
    }

    @GetMapping("/neu")
    public String neu(Model model) {
        return formular(model, new MaschineFormular(), null);
    }

    @PostMapping
    public String anlegen(@ModelAttribute("formular") MaschineFormular formular, BindingResult ergebnis,
            Model model, RedirectAttributes weiterleitung) {
        pruefung.pruefe(formular.alsRequest(), ergebnis);
        if (!ergebnis.hasErrors()) {
            try {
                var neu = service.anlegen(formular.alsRequest());
                weiterleitung.addFlashAttribute("meldung",
                        "Maschine " + neu.inventarnummer() + " (" + neu.bezeichnung() + ") wurde angelegt.");
                return "redirect:/maschinen";
            } catch (ResponseStatusException e) {
                pruefung.uebernehme(e, ergebnis, FELDER);
            }
        }
        return formular(model, formular, null);
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeiten(@PathVariable Long id, Model model) {
        return formular(model, MaschineFormular.von(service.finde(id)), id);
    }

    @PostMapping("/{id}")
    public String aendern(@PathVariable Long id, @ModelAttribute("formular") MaschineFormular formular,
            BindingResult ergebnis, Model model, RedirectAttributes weiterleitung) {
        pruefung.pruefe(formular.alsRequest(), ergebnis);
        if (!ergebnis.hasErrors()) {
            try {
                var neu = service.aendern(id, formular.alsRequest());
                weiterleitung.addFlashAttribute("meldung",
                        "Änderungen an " + neu.inventarnummer() + " wurden gespeichert und protokolliert.");
                return "redirect:/maschinen";
            } catch (ResponseStatusException e) {
                pruefung.uebernehme(e, ergebnis, FELDER);
            }
        }
        return formular(model, formular, id);
    }

    @PostMapping("/{id}/loeschen")
    public String loeschen(@PathVariable Long id, RedirectAttributes weiterleitung) {
        try {
            var maschine = service.finde(id);
            service.loeschen(id);
            weiterleitung.addFlashAttribute("meldung", "Maschine " + maschine.inventarnummer() + " wurde gelöscht.");
        } catch (ResponseStatusException e) {
            weiterleitung.addFlashAttribute("fehlermeldung", e.getReason());
        }
        return "redirect:/maschinen";
    }

    @GetMapping("/{id}/historie")
    public String historie(@PathVariable Long id, Model model) {
        model.addAttribute("maschine", service.finde(id));
        model.addAttribute("eintraege", service.historie(id).stream().map(AuditAnzeige::von).toList());
        return "maschinen/historie";
    }

    @GetMapping("/import")
    public String importSeite(Model model) {
        model.addAttribute("seite", "import");
        return "maschinen/import";
    }

    @PostMapping("/import")
    public String importieren(@RequestParam(value = "datei", required = false) MultipartFile datei, Model model) {
        model.addAttribute("seite", "import");
        try {
            model.addAttribute("ergebnis", importService.importiere(datei));
            model.addAttribute("dateiname", datei.getOriginalFilename());
        } catch (ResponseStatusException e) {
            model.addAttribute("fehler", e.getReason());
        }
        return "maschinen/import";
    }

    private String formular(Model model, MaschineFormular formular, Long id) {
        model.addAttribute("formular", formular);
        model.addAttribute("maschineId", id);
        model.addAttribute("kategorien", kategorieService.alle());
        model.addAttribute("standorte", standortService.alle());
        model.addAttribute("statuswerte", MaschinenStatus.values());
        return "maschinen/formular";
    }
}
