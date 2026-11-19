package de.projektarbeit.maschinenverleih.service.kundenimport;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.projektarbeit.maschinenverleih.dto.KundeRequest;
import de.projektarbeit.maschinenverleih.dto.KundeResponse;
import de.projektarbeit.maschinenverleih.model.Kundentyp;

/** Unit-Tests fuer die Dubletten- und Wiederholungserkennung - ohne Datenbank. */
class KundenAbgleichTest {

    private static final KundeResponse ELEKTRO_KLEIN = new KundeResponse(1L, "K-1036", Kundentyp.GEWERBE,
            "Elektro Klein GmbH", null, null, "Klein", "Elektro Klein GmbH (Klein)", "Sehr geehrte Damen und Herren",
            "info@elektro-klein.de",
            "+49 2625 991014", "Kirchweg 52", "42899", "Remscheid", null, null, null, LocalDateTime.now());

    private final KundenAbgleich abgleich = new KundenAbgleich(List.of(ELEKTRO_KLEIN));

    private static KundeRequest kunde(String nummer, String firma, String email, String strasse, String plz) {
        return new KundeRequest(nummer, Kundentyp.GEWERBE, firma, null, null, "Klein", email, null, strasse, plz,
                "Remscheid", null, null, null);
    }

    @Test
    void gleicheNummerUndDerselbeKundeIstBereitsVorhanden() {
        var entscheidung = abgleich.pruefe(kunde("K-1036", "Elektro Klein GmbH", "info@elektro-klein.de",
                "Kirchweg 52", "42899"));

        assertThat(entscheidung).isEqualTo(new KundenAbgleich.BereitsVorhanden("K-1036"));
    }

    @Test
    void gleicheNummerAberAndererKundeIstEinKonflikt() {
        var entscheidung = abgleich.pruefe(kunde("K-1036", "Tiefbau Wagner OHG", "info@tiefbau-wagner.de",
                "Ringstr. 22", "42651"));

        assertThat(entscheidung).isInstanceOf(KundenAbgleich.Konflikt.class);
    }

    @Test
    void andereNummerAberGleicheAnschriftUndEmailIstEineDublette() {
        // K-1111 im Altsystem: "vermutlich Dublette zu K-1036", Strasse anders geschrieben
        var entscheidung = abgleich.pruefe(kunde("K-1111", "Elektro Klein GmbH", "INFO@elektro-klein.de",
                "Kirchweg 52", "42899"));

        assertThat(entscheidung).isInstanceOf(KundenAbgleich.Dublette.class);
        assertThat(((KundenAbgleich.Dublette) entscheidung).meldung()).contains("K-1036");
    }

    @Test
    void schreibweiseVonStrasseUndUmlautenSpieltKeineRolle() {
        var vorhanden = new KundenAbgleich(List.of(new KundeResponse(2L, "K-1018", Kundentyp.GEWERBE,
                "Kanalbau Krueger GbR", null, null, "Krüger", "Kanalbau Krueger GbR (Krüger)",
                "Sehr geehrte Damen und Herren", "info@kanalbau-krüger.de",
                null, "Hauptstraße 17", "42897", "Remscheid", null, null, null, LocalDateTime.now())));

        var entscheidung = vorhanden.pruefe(kunde("K-1114", "Kanalbau Krüger GbR", "info@kanalbau-krüger.de",
                "Hauptstr. 17", "42897"));

        assertThat(entscheidung).isInstanceOf(KundenAbgleich.Dublette.class);
    }

    @Test
    void gleicheEmailAberAndereAnschriftMussGeklaertWerden() {
        // K-1037 "Elektro Klein UG" in Solingen mit derselben E-Mail wie K-1036 in Remscheid
        var entscheidung = abgleich.pruefe(kunde("K-1037", "Elektro Klein UG", "info@elektro-klein.de",
                "Mühlenweg 26", "42651"));

        assertThat(entscheidung).isInstanceOf(KundenAbgleich.Konflikt.class);
    }

    @Test
    void unbekannterKundeIstNeu() {
        var entscheidung = abgleich.pruefe(kunde("K-1099", "Tiefbau Wagner OHG", "info@tiefbau-wagner.de",
                "Ringstr. 22", "42651"));

        assertThat(entscheidung).isInstanceOf(KundenAbgleich.Neu.class);
    }

    @Test
    void neueNummerSchliesstAnAlleBelegtenNummernAn() {
        // Nummern aus der Datei zaehlen mit, auch wenn die Zeile (noch) nicht importiert wurde
        abgleich.reserviere(List.of("K-1001", "K-1118"));

        assertThat(abgleich.naechsteKundennummer()).isEqualTo("K-1119");
    }
}
