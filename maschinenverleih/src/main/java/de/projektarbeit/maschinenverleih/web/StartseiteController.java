package de.projektarbeit.maschinenverleih.web;

import java.time.LocalDate;
import java.util.Comparator;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import de.projektarbeit.maschinenverleih.dto.ReservierungResponse;
import de.projektarbeit.maschinenverleih.model.MaschinenStatus;
import de.projektarbeit.maschinenverleih.model.ReservierungsStatus;
import de.projektarbeit.maschinenverleih.service.KategorieService;
import de.projektarbeit.maschinenverleih.service.KundeService;
import de.projektarbeit.maschinenverleih.service.MaschineService;
import de.projektarbeit.maschinenverleih.service.ReservierungService;
import de.projektarbeit.maschinenverleih.service.StandortService;

/** Startseite mit Kennzahlen, Schnellsuche und den naechsten Reservierungen. */
@Controller
public class StartseiteController {

    private final MaschineService maschineService;
    private final ReservierungService reservierungService;
    private final KundeService kundeService;
    private final KategorieService kategorieService;
    private final StandortService standortService;

    public StartseiteController(MaschineService maschineService, ReservierungService reservierungService,
            KundeService kundeService, KategorieService kategorieService, StandortService standortService) {
        this.maschineService = maschineService;
        this.reservierungService = reservierungService;
        this.kundeService = kundeService;
        this.kategorieService = kategorieService;
        this.standortService = standortService;
    }

    @GetMapping("/")
    public String start(Model model) {
        LocalDate heute = LocalDate.now();
        var maschinen = maschineService.alle(null, null);
        var aktive = reservierungService.alle(null).stream()
                .filter(r -> r.status() == ReservierungsStatus.BESTAETIGT && !r.bis().isBefore(heute))
                .sorted(Comparator.comparing(ReservierungResponse::von))
                .toList();

        model.addAttribute("seite", "start");
        model.addAttribute("anzahlMaschinen", maschinen.size());
        model.addAttribute("heuteFrei", maschineService.verfuegbare(null, null, heute, heute).size());
        model.addAttribute("inWartung",
                maschinen.stream().filter(m -> m.status() == MaschinenStatus.IN_WARTUNG).count());
        model.addAttribute("aktiveReservierungen", aktive.size());
        model.addAttribute("anzahlKunden", kundeService.alle().size());
        model.addAttribute("naechste", aktive.stream().limit(5).toList());
        model.addAttribute("kategorien", kategorieService.alle());
        model.addAttribute("standorte", standortService.alle());
        model.addAttribute("heute", heute);
        return "index";
    }
}
