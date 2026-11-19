package de.projektarbeit.maschinenverleih.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.dto.AuditLogResponse;
import de.projektarbeit.maschinenverleih.dto.KundeRequest;
import de.projektarbeit.maschinenverleih.dto.KundeResponse;
import de.projektarbeit.maschinenverleih.model.Anrede;
import de.projektarbeit.maschinenverleih.model.AuditAktion;

/**
 * Integrationstest mit echter Datenbank (in der CI: Wegwerf-PostgreSQL mit schema.sql + Migrationen).
 * {@code @Transactional}: Am Ende jedes Tests wird alles zurueckgerollt - es bleiben keine Testdaten zurueck.
 */
@SpringBootTest
@Transactional
class KundenImportIntegrationTest {

    @Autowired
    private KundenImportService importService;

    @Autowired
    private KundeService kundeService;

    /** Kleine Altdaten-Datei im Format des alten Systems (Tabellenblatt "Kunden_ALT") */
    private static MockMultipartFile altdaten() throws IOException {
        String[][] zeilen = {
                {"KdNr", "Kunde / Ansprechpartner", "Adresse", "Telefon", "E-Mail", "Kundentyp", "Kunde seit", "Anmerkung"},
                {"K-9001", "Testbau Krause GmbH / Fr. Krause", "Talstrasse 2 42651 Sollingen", "02574 / 35 93 99",
                        "info@testbau-krause.example, buchhaltung@testbau-krause.example", "Gewerbekunde", "21.06.2016", ""},
                {"K-9002", "Birgit Testmann", "Parkstr. 28 42855 Remschied", "", "birgit@testmann.example", "PK",
                        "08.10.2020", "Großkunde"},
                {"", "Julia Testmann", "42859 Remscheid, Nordring 25", "02379-472528", "julia@testmann.example",
                        "Privat", "29.12.2016", "KdNr fehlt im Altsystem"},
                {"K-9004", "Kevin Testmann", "Schulstr. 42", "0176-9996207", "kevin@testmann.example", "Privatkunde",
                        "18.11.2024", ""},
                {"K-9005", "Testbau Krause GmbH (Herr Krause)", "Talstraße 2, 42651 Solingen", "0160 5227787",
                        "info@testbau-krause.example", "Firma", "21.06.2016", "vermutlich Dublette zu K-9001"},
        };
        try (var workbook = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            workbook.createSheet("Verleih_Abrechnung_ALT").createRow(0).createCell(0).setCellValue("Rechnungsnr");
            var blatt = workbook.createSheet("Kunden_ALT");
            for (int i = 0; i < zeilen.length; i++) {
                var row = blatt.createRow(i);
                for (int j = 0; j < zeilen[i].length; j++) {
                    row.createCell(j).setCellValue(zeilen[i][j]);
                }
            }
            workbook.write(out);
            return new MockMultipartFile("datei", "altdaten.xlsx", null, out.toByteArray());
        }
    }

    @Test
    void importLegtGueltigeKundenAnUndProtokolliertDenRest() throws IOException {
        var ergebnis = importService.importiere(altdaten());

        assertThat(ergebnis.importiert()).isEqualTo(3);              // K-9001, K-9002, Julia (neue Nummer)
        assertThat(ergebnis.fehler()).singleElement()                 // K-9004: PLZ und Ort fehlen
                .satisfies(f -> {
                    assertThat(f.zeile()).isEqualTo(5);
                    assertThat(f.meldung()).contains("PLZ und Ort fehlen");
                });
        assertThat(ergebnis.dubletten()).singleElement()              // K-9005 = K-9001
                .satisfies(d -> assertThat(d.meldung()).contains("K-9001").contains("0160 5227787"));

        var krause = kundeService.suche("K-9001").getFirst();
        assertThat(krause.firma()).isEqualTo("Testbau Krause GmbH");
        assertThat(krause.nachname()).isEqualTo("Krause");
        assertThat(krause.anrede()).isEqualTo(Anrede.FRAU);                     // "Fr. Krause"
        assertThat(krause.briefanrede()).isEqualTo("Sehr geehrte Frau Krause");
        assertThat(krause.ort()).isEqualTo("Solingen");
        assertThat(krause.telefon()).isEqualTo("02574 359399");
        assertThat(krause.bemerkung()).contains("buchhaltung@testbau-krause.example");
        assertThat(kundeService.historie(krause.id())).singleElement()
                .satisfies(eintrag -> assertThat(eintrag.aktion()).isEqualTo(AuditAktion.IMPORTIERT));

        var julia = kundeService.suche("julia@testmann.example").getFirst();
        // naechste freie Nummer: K-9005 steht in der Datei (Dublette) und gilt deshalb als belegt
        assertThat(julia.kundennummer()).isEqualTo("K-9006");
    }

    @Test
    void zweiterImportErzeugtKeineDuplikate() throws IOException {
        importService.importiere(altdaten());
        int anzahlNachErstemImport = kundeService.alle().size();

        var zweiterLauf = importService.importiere(altdaten());

        assertThat(zweiterLauf.importiert()).isZero();
        assertThat(zweiterLauf.bereitsVorhanden()).isEqualTo(2);     // K-9001, K-9002
        assertThat(zweiterLauf.dubletten()).hasSize(2);               // Julia (ohne Nummer) und K-9005
        assertThat(kundeService.alle()).hasSize(anzahlNachErstemImport);
    }

    @Test
    void importierteKundenLassenSichAendernUndSuchen() throws IOException {
        importService.importiere(altdaten());
        var birgit = kundeService.suche("testmann").stream()
                .filter(k -> "Birgit".equals(k.vorname())).findFirst().orElseThrow();

        var geaendert = kundeService.aendern(birgit.id(), new KundeRequest(birgit.kundennummer(),
                birgit.kundentyp(), null, Anrede.FRAU, "Birgit", "Testmann-Neu", birgit.email(), "0521 123456",
                birgit.strasse(), birgit.plz(), birgit.ort(), null, birgit.kundeSeit(), birgit.bemerkung()));

        assertThat(geaendert.nachname()).isEqualTo("Testmann-Neu");
        assertThat(geaendert.anrede()).isEqualTo(Anrede.FRAU);
        assertThat(kundeService.suche("testmann-neu")).extracting(KundeResponse::id).containsExactly(birgit.id());
        assertThat(kundeService.historie(birgit.id())).extracting(AuditLogResponse::aktion)
                .containsExactlyInAnyOrder(AuditAktion.IMPORTIERT, AuditAktion.GEAENDERT);
    }

    @Test
    void dateiOhneKundenblattWirdAbgelehnt() {
        var falscheDatei = new MockMultipartFile("datei", "x.xlsx", null, new byte[] {1, 2, 3});

        assertThatThrownBy(() -> importService.importiere(falscheDatei))
                .isInstanceOf(ResponseStatusException.class);
    }
}
