package de.projektarbeit.maschinenverleih.service.kundenimport;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.projektarbeit.maschinenverleih.model.Anrede;
import de.projektarbeit.maschinenverleih.model.Kundentyp;

/**
 * Unit-Tests fuer die Aufbereitung der Altdaten - ohne Datenbank und ohne Spring.
 * Die Beispiele stammen aus der echten Altdaten-Datei (Tabellenblatt Kunden_ALT).
 */
class AltkundenAufbereitungTest {

    private final AltkundenAufbereitung aufbereitung = new AltkundenAufbereitung();

    private static AltkundeRoh zeile(String kdnr, String name, String adresse, String telefon, String email,
            String typ, String seit, String anmerkung) {
        return new AltkundeRoh(2, kdnr, name, adresse, telefon, email, typ, seit, anmerkung);
    }

    private AufbereiteterAltkunde privat(String name) {
        return aufbereitung.bereiteAuf(zeile("K-1002", name, "Parkstr. 28 42855 Remscheid", "02094 / 58 22 55",
                "birgit.krause@t-online.de", "privat", "08.10.2020", null));
    }

    private AufbereiteterAltkunde gewerbe(String name) {
        return aufbereitung.bereiteAuf(zeile("K-1001", name, "Am Hang 36, 42651 Solingen", "0171-2556017",
                "info@firma.de", "Gewerbekunde", "21.06.2016", null));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "GaLaBau Krause GmbH & Co. KG / Fr. Krause        | GaLaBau Krause GmbH & Co. KG | FRAU | Krause",
            "Dachdecker Hartmann UG, Frau Hartmann            | Dachdecker Hartmann UG       | FRAU | Hartmann",
            "Elektro Neumann GbR (Hr. Neumann)                | Elektro Neumann GbR          | HERR | Neumann",
            "Frau Weber, Dachdecker Weber GmbH & Co. KG       | Dachdecker Weber GmbH & Co. KG | FRAU | Weber",
            "Bauer, Abbruch Bauer eK                          | Abbruch Bauer e.K.           |      | Bauer",
            "Geruestbau Krämer GbR - z. Hd. Hr. Krämer        | Geruestbau Krämer GbR        | HERR | Krämer",
            "Elektro Klein GmbH (Ansprechpartner: Herr Klein) | Elektro Klein GmbH           | HERR | Klein",
            "Trockenbau Neumann G.m.b.H. & Co. KG, Neumann    | Trockenbau Neumann GmbH & Co. KG |  | Neumann",
            "Abbruch Werner Gmbh & Co. KG / Frau Werner       | Abbruch Werner GmbH & Co. KG | FRAU | Werner",
            "Maler Becker e.K. - z. Hd. Becker                | Maler Becker e.K.            |      | Becker",
            "Bau Wolf GmbH - z. Hd. Herrn Wolf                | Bau Wolf GmbH                | HERR | Wolf"
    })
    void firmaUndAnsprechpartnerWerdenGetrennt(String altdaten, String firma, Anrede anrede, String nachname) {
        var ergebnis = gewerbe(altdaten);

        assertThat(ergebnis.fehler()).isEmpty();
        assertThat(ergebnis.request().firma()).isEqualTo(firma);
        assertThat(ergebnis.request().anrede()).isEqualTo(anrede);
        assertThat(ergebnis.request().nachname()).isEqualTo(nachname);
        assertThat(ergebnis.request().vorname()).isNull();
        assertThat(ergebnis.request().kundentyp()).isEqualTo(Kundentyp.GEWERBE);
    }

    @Test
    void privatkundeMitVorUndNachname() {
        var request = privat("Birgit Krause").request();

        assertThat(request.firma()).isNull();
        assertThat(request.vorname()).isEqualTo("Birgit");
        assertThat(request.nachname()).isEqualTo("Krause");
        assertThat(request.kundentyp()).isEqualTo(Kundentyp.PRIVAT);
        assertThat(request.anrede()).as("aus dem Vornamen wird nicht auf Frau/Herr geschlossen").isNull();
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "Frau Birgit Krause    | FRAU",
            "Krause, Frau Birgit   | FRAU",
            "Hr. Krause, Uwe       | HERR",
            "Herrn Uwe Krause      | HERR"
    })
    void anredeBeiPrivatkundenWirdUebernommen(String altdaten, Anrede anrede) {
        var request = privat(altdaten).request();

        assertThat(request.anrede()).isEqualTo(anrede);
        assertThat(request.nachname()).isEqualTo("Krause");
        assertThat(request.vorname()).isIn("Birgit", "Uwe");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "Talstrasse 2 42651 Sollingen     | Talstrasse 2      | 42651 | Solingen",
            "Am Hang 36, 42651 Solingen       | Am Hang 36        | 42651 | Solingen",
            "42859 Remscheid, Nordring 25     | Nordring 25       | 42859 | Remscheid",
            "Poststraße 35; 42579 Heiligenhaus | Poststraße 35    | 42579 | Heiligenhaus",
            "42111 W-tal, Industriestr. 39    | Industriestr. 39  | 42111 | Wuppertal",
            "Fabrikstr. 7 42651 SG            | Fabrikstr. 7      | 42651 | Solingen",
            "Fabrikstraße 8, 51379 LEVERKUSEN | Fabrikstraße 8    | 51379 | Leverkusen"
    })
    void anschriftWirdZerlegtUndOrtKorrigiert(String altdaten, String strasse, String plz, String ort) {
        var request = aufbereitung.bereiteAuf(zeile("K-1001", "Birgit Krause", altdaten, null, "a@b.de",
                "privat", null, null)).request();

        assertThat(request.strasse()).isEqualTo(strasse);
        assertThat(request.plz()).isEqualTo(plz);
        assertThat(request.ort()).isEqualTo(ort);
    }

    @Test
    void anschriftMitZeilenumbruch() {
        var request = aufbereitung.bereiteAuf(zeile("K-1023", "Birgit Krause", "Parkstraße 20\n42111 Wuppertal",
                null, "a@b.de", "privat", null, null)).request();

        assertThat(request.strasse()).isEqualTo("Parkstraße 20");
        assertThat(request.ort()).isEqualTo("Wuppertal");
    }

    @Test
    void ortskorrekturWirdAlsHinweisProtokolliert() {
        var ergebnis = aufbereitung.bereiteAuf(zeile("K-1002", "Birgit Krause", "Parkstr. 28 42855 Remschied",
                null, "a@b.de", "privat", null, null));

        assertThat(ergebnis.hinweise()).containsExactly("Ort 'Remschied' korrigiert zu 'Remscheid'");
    }

    @Test
    void unvollstaendigeAnschriftIstEinFehler() {
        var ergebnis = aufbereitung.bereiteAuf(zeile("K-1030", "Kevin Hartmann", "Schulstr. 42", "0176-9996207",
                "kevin.hartmann@freenet.de", "Privatkunde", "18.11.2024", null));

        assertThat(ergebnis.hatFehler()).isTrue();
        assertThat(ergebnis.request()).isNull();
        assertThat(ergebnis.fehler()).singleElement().asString().contains("PLZ und Ort fehlen");
    }

    @Test
    void ersteTelefonnummerInsFeldWeitereUndFaxInDieBemerkung() {
        var request = aufbereitung.bereiteAuf(zeile("K-1014", "Abbruch Schröder Gmbh (Herr Schröder)",
                "Industriestr. 5, 42859 Remscheid", "02880 / 52 19 75 / 0171-4860684, Fax 02724-761759",
                "info@abbruch-schroeder.de", "Gewerbekunde", null, null)).request();

        assertThat(request.telefon()).isEqualTo("02880 521975");
        assertThat(request.bemerkung()).isEqualTo("Weitere Telefonnummern: 0171 4860684; Fax: 02724 761759");
    }

    @Test
    void telefonnummernInMehrerenZeilenVerschmelzenNicht() {
        var request = aufbereitung.bereiteAuf(zeile("K-1006", "Frau Weber, Dachdecker Weber GmbH & Co. KG",
                "Gartenstr. 26, 42897 Remscheid", "0178/6279418\n0151/6440561", "info@dachdecker-weber.de",
                "Gewerbekunde", null, null)).request();

        assertThat(request.telefon()).isEqualTo("0178 6279418");
        assertThat(request.bemerkung()).isEqualTo("Weitere Telefonnummern: 0151 6440561");
    }

    @Test
    void ersteEmailInsFeldWeitereInDieBemerkung() {
        var request = aufbereitung.bereiteAuf(zeile("K-1003", "Dachdecker Hartmann UG, Frau Hartmann",
                "Am Hang 36, 42651 Solingen", null,
                "info@dachdecker-hartmann.de / buchhaltung@dachdecker-hartmann.de", "Gewerbe", null, null)).request();

        assertThat(request.email()).isEqualTo("info@dachdecker-hartmann.de");
        assertThat(request.bemerkung()).isEqualTo("Weitere E-Mail-Adressen: buchhaltung@dachdecker-hartmann.de");
    }

    @Test
    void kundentypPasstNichtZumNamenIstEinFehler() {
        var ergebnis = aufbereitung.bereiteAuf(zeile("K-1099", "Tiefbau Wagner OHG / Frau Wagner",
                "Ringstr. 22, 42651 Solingen", null, "info@tiefbau-wagner.de", "privat", null, null));

        assertThat(ergebnis.fehler()).singleElement().asString().contains("passt nicht zum Namen");
    }

    @Test
    void nurJahrBeiKundeSeitWirdZumErstenJanuarMitHinweis() {
        var ergebnis = aufbereitung.bereiteAuf(zeile("K-1055", "Bernd Wolf", "Poststr. 38, 42899 Remscheid",
                null, "bernd.wolf@t-online.de", "Privatkunde", "2017", null));

        assertThat(ergebnis.request().kundeSeit()).isEqualTo(LocalDate.of(2017, 1, 1));
        assertThat(ergebnis.hinweise()).singleElement().asString().contains("nur das Jahr 2017");
    }

    @Test
    void fachlicheAnmerkungBleibtImportnotizenNicht() {
        var ergebnis = aufbereitung.bereiteAuf(zeile(null, "Julia Hartmann", "42859 Remscheid, Nordring 25",
                null, "julia.hartmann@t-online.de", "Privat", "29.12.2016",
                "KdNr fehlt im Altsystem; zahlt oft verspätet"));

        assertThat(ergebnis.request().bemerkung()).isEqualTo("Altsystem: zahlt oft verspätet");
        assertThat(ergebnis.ohneKundennummer()).isTrue();
    }
}
