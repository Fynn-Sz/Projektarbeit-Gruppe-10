package de.projektarbeit.maschinenverleih.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.dto.MaschineResponse;
import de.projektarbeit.maschinenverleih.dto.ReservierungRequest;
import de.projektarbeit.maschinenverleih.service.KategorieService;
import de.projektarbeit.maschinenverleih.service.KundeService;
import de.projektarbeit.maschinenverleih.service.MaschineService;
import de.projektarbeit.maschinenverleih.service.ReservierungService;
import de.projektarbeit.maschinenverleih.service.StandortService;

/**
 * US 1: Verfuegbarkeitssuche -> Maschine auswaehlen -> verbindlich reservieren -> Bestaetigung.
 */
@Controller
public class ReservierungSeitenController {

    /** Ein Suchtreffer mit dem Gesamtpreis fuer den gesuchten Zeitraum. */
    public record Treffer(MaschineResponse maschine, BigDecimal gesamtpreis) {
    }

    private final MaschineService maschineService;
    private final ReservierungService reservierungService;
    private final KundeService kundeService;
    private final KategorieService kategorieService;
    private final StandortService standortService;
    private final Formularpruefung pruefung;

    public ReservierungSeitenController(MaschineService maschineService, ReservierungService reservierungService,
            KundeService kundeService, KategorieService kategorieService, StandortService standortService,
            Formularpruefung pruefung) {
        this.maschineService = maschineService;
        this.reservierungService = reservierungService;
        this.kundeService = kundeService;
        this.kategorieService = kategorieService;
        this.standortService = standortService;
        this.pruefung = pruefung;
    }

    @GetMapping("/suche")
    public String suche(@RequestParam(required = false) Long kategorieId,
            @RequestParam(required = false) Long standortId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            Model model) {
        LocalDate heute = LocalDate.now();
        model.addAttribute("seite", "suche");
        model.addAttribute("kategorien", kategorieService.alle());
        model.addAttribute("standorte", standortService.alle());
        model.addAttribute("kategorieId", kategorieId);
        model.addAttribute("standortId", standortId);
        model.addAttribute("von", von);
        model.addAttribute("bis", bis);
        model.addAttribute("heute", heute);

        if (von == null && bis == null) {
            return "suche"; // Seite zum ersten Mal geoeffnet - noch keine Suche
        }
        if (von == null || bis == null) {
            model.addAttribute("fehler", "Bitte Start- und Enddatum angeben.");
        } else if (von.isBefore(heute)) {
            model.addAttribute("fehler", "Das Startdatum darf nicht in der Vergangenheit liegen.");
        } else {
            try {
                long tage = mietTage(von, bis);
                var treffer = maschineService.verfuegbare(kategorieId, standortId, von, bis).stream()
                        .map(m -> new Treffer(m, m.tagesmietpreis().multiply(BigDecimal.valueOf(tage))))
                        .toList();
                model.addAttribute("treffer", treffer);
                model.addAttribute("tage", tage);
            } catch (ResponseStatusException e) {
                model.addAttribute("fehler", e.getReason());
            }
        }
        return "suche";
    }

    @GetMapping("/reservieren")
    public String reservierenFormular(@RequestParam Long maschineId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            @RequestParam(required = false) Long kundeId, Model model) {
        befuelleReservierung(model, maschineId, von, bis, kundeId);
        return "reservierungen/reservieren";
    }

    @PostMapping("/reservieren")
    public String reservieren(@RequestParam Long maschineId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            @RequestParam(required = false) Long kundeId, Model model, RedirectAttributes weiterleitung) {
        var request = new ReservierungRequest(kundeId, maschineId, von, bis);
        var meldungen = pruefung.meldungen(request);
        if (!meldungen.isEmpty()) {
            model.addAttribute("fehler", meldungen);
            befuelleReservierung(model, maschineId, von, bis, kundeId);
            return "reservierungen/reservieren";
        }
        try {
            var reservierung = reservierungService.reservieren(request);
            weiterleitung.addFlashAttribute("neu", true);
            return "redirect:/reservierungen/" + reservierung.id();
        } catch (ResponseStatusException e) {
            // z. B. 409: jemand anderes war schneller - Meldung auf derselben Seite anzeigen
            model.addAttribute("fehler", List.of(e.getReason()));
            befuelleReservierung(model, maschineId, von, bis, kundeId);
            return "reservierungen/reservieren";
        }
    }

    @GetMapping("/reservierungen")
    public String reservierungen(@RequestParam(required = false) Long kundeId, Model model) {
        model.addAttribute("seite", "reservierungen");
        model.addAttribute("reservierungen", reservierungService.alle(kundeId));
        model.addAttribute("kunden", kundeService.alle());
        model.addAttribute("kundeId", kundeId);
        model.addAttribute("heute", LocalDate.now());
        return "reservierungen/liste";
    }

    @GetMapping("/reservierungen/{id}")
    public String reservierung(@PathVariable Long id, Model model) {
        model.addAttribute("seite", "reservierungen");
        model.addAttribute("reservierung", reservierungService.finde(id));
        return "reservierungen/detail";
    }

    @PostMapping("/reservierungen/{id}/stornieren")
    public String stornieren(@PathVariable Long id, RedirectAttributes weiterleitung) {
        try {
            reservierungService.stornieren(id);
            weiterleitung.addFlashAttribute("meldung",
                    "Reservierung #" + id + " wurde storniert. Die Maschine ist im Zeitraum wieder frei.");
        } catch (ResponseStatusException e) {
            weiterleitung.addFlashAttribute("fehlermeldung", e.getReason());
        }
        return "redirect:/reservierungen/" + id;
    }

    private void befuelleReservierung(Model model, Long maschineId, LocalDate von, LocalDate bis, Long kundeId) {
        var maschine = maschineService.finde(maschineId);
        long tage = Math.max(mietTage(von, bis), 0);
        model.addAttribute("seite", "suche");
        model.addAttribute("maschine", maschine);
        model.addAttribute("von", von);
        model.addAttribute("bis", bis);
        model.addAttribute("tage", tage);
        model.addAttribute("gesamtpreis", maschine.tagesmietpreis().multiply(BigDecimal.valueOf(tage)));
        model.addAttribute("kunden", kundeService.alle());
        model.addAttribute("kundeId", kundeId);
    }

    // von und bis zaehlen beide als Miettag (wie in ReservierungResponse)
    private static long mietTage(LocalDate von, LocalDate bis) {
        return ChronoUnit.DAYS.between(von, bis) + 1;
    }
}
