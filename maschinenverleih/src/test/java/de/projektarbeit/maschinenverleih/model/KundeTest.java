package de.projektarbeit.maschinenverleih.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit-Tests fuer die Briefanrede - ohne Datenbank und ohne Spring. */
class KundeTest {

    private static Kunde kunde(String firma, Anrede anrede, String vorname, String nachname) {
        var kunde = new Kunde();
        kunde.setFirma(firma);
        kunde.setAnrede(anrede);
        kunde.setVorname(vorname);
        kunde.setNachname(nachname);
        return kunde;
    }

    @Test
    void mitAnredeWirdPersoenlichAngeschrieben() {
        assertThat(kunde("GaLaBau Krause GmbH", Anrede.FRAU, null, "Krause").getBriefanrede())
                .isEqualTo("Sehr geehrte Frau Krause");
        assertThat(kunde(null, Anrede.HERR, "Uwe", "Krause").getBriefanrede())
                .isEqualTo("Sehr geehrter Herr Krause");
    }

    @Test
    void ohneAnredeWirdNeutralAngeschrieben() {
        assertThat(kunde(null, null, "Birgit", "Krause").getBriefanrede())
                .isEqualTo("Guten Tag Birgit Krause");
        assertThat(kunde("Maler Becker e.K.", null, null, "Becker").getBriefanrede())
                .isEqualTo("Sehr geehrte Damen und Herren");
    }
}
