package de.projektarbeit.maschinenverleih.service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.poi.EmptyFileException;
import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.dto.KundeRequest;
import de.projektarbeit.maschinenverleih.dto.KundenImportErgebnis;
import de.projektarbeit.maschinenverleih.dto.KundenImportErgebnis.Zeilenmeldung;
import de.projektarbeit.maschinenverleih.model.AuditAktion;
import de.projektarbeit.maschinenverleih.service.kundenimport.AltkundeRoh;
import de.projektarbeit.maschinenverleih.service.kundenimport.AltkundenAufbereitung;
import de.projektarbeit.maschinenverleih.service.kundenimport.AufbereiteterAltkunde;
import de.projektarbeit.maschinenverleih.service.kundenimport.KundenAbgleich;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Importschnittstelle fuer die Kundendaten aus dem alten Excel-System.
 *
 * Ablauf:
 *  1. Tabellenblatt mit den Kunden finden (Spalten KdNr, Kunde / Ansprechpartner, Adresse ...) -
 *     die komplette Altdaten-Datei kann also unveraendert hochgeladen werden.
 *  2. Jede Zeile aufbereiten (AltkundenAufbereitung): Felder zerlegen, Schreibfehler korrigieren.
 *  3. Mit vorhandenen Kunden abgleichen (KundenAbgleich): neu / schon vorhanden / Dublette / Konflikt.
 *  4. Neue Kunden einzeln speichern (Audit-Trail: IMPORTIERT) und alles im Ergebnis protokollieren.
 * Jede Zeile wird fuer sich gespeichert: Eine fehlerhafte Zeile haelt die anderen nicht auf.
 */
@Service
public class KundenImportService {

    private static final Logger LOG = LoggerFactory.getLogger(KundenImportService.class);

    /** Feld -> erlaubte Spaltenueberschriften (vereinfacht: klein, ohne Leer- und Sonderzeichen) */
    private static final Map<String, List<String>> UEBERSCHRIFTEN = new LinkedHashMap<>();

    static {
        UEBERSCHRIFTEN.put("kundennummer", List.of("kdnr", "kundennr", "kundennummer"));
        UEBERSCHRIFTEN.put("name", List.of("kundeansprechpartner", "kunde", "name", "kundenname"));
        UEBERSCHRIFTEN.put("adresse", List.of("adresse", "anschrift"));
        UEBERSCHRIFTEN.put("telefon", List.of("telefon", "tel", "telefonnummer"));
        UEBERSCHRIFTEN.put("email", List.of("email", "emailadresse", "mail"));
        UEBERSCHRIFTEN.put("kundentyp", List.of("kundentyp", "typ"));
        UEBERSCHRIFTEN.put("kundeseit", List.of("kundeseit"));
        UEBERSCHRIFTEN.put("anmerkung", List.of("anmerkung", "bemerkung"));
    }

    private static final List<String> PFLICHTSPALTEN = List.of("kundennummer", "name", "adresse");
    private static final DateTimeFormatter DEUTSCHES_DATUM = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final KundeService kundeService;
    private final Validator validator;
    private final AltkundenAufbereitung aufbereitung = new AltkundenAufbereitung();
    private final DataFormatter formatter = new DataFormatter(Locale.GERMANY);

    public KundenImportService(KundeService kundeService, Validator validator) {
        this.kundeService = kundeService;
        this.validator = validator;
    }

    public KundenImportErgebnis importiere(MultipartFile datei) {
        if (datei == null || datei.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es wurde keine Datei hochgeladen");
        }
        try (Workbook workbook = WorkbookFactory.create(datei.getInputStream())) {
            Sheet blatt = findeKundenblatt(workbook);
            LOG.info("Kunden-Import aus '{}', Tabellenblatt '{}'", datei.getOriginalFilename(), blatt.getSheetName());
            return importiere(blatt, leseKopfzeile(blatt.getRow(0)));
        } catch (IOException | EmptyFileException | UnsupportedFileFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Datei konnte nicht gelesen werden – bitte eine Excel-Datei (.xlsx) hochladen");
        }
    }

    private KundenImportErgebnis importiere(Sheet blatt, Map<String, Integer> spalten) {
        // 1. alle Zeilen aufbereiten
        List<AufbereiteterAltkunde> zeilen = new ArrayList<>();
        for (int i = 1; i <= blatt.getLastRowNum(); i++) {
            Row row = blatt.getRow(i);
            if (!istLeer(row)) {
                zeilen.add(aufbereitung.bereiteAuf(leseZeile(row, i + 1, spalten)));
            }
        }

        // 2. Abgleich vorbereiten: vorhandene Kunden + alle Nummern aus der Datei gelten als belegt
        var abgleich = new KundenAbgleich(kundeService.alle());
        abgleich.reserviere(zeilen.stream().map(KundenImportService::kundennummer).filter(Objects::nonNull).toList());

        // 3. Erst Zeilen MIT Kundennummer, dann die ohne: So gewinnt bei einer Dublette der Datensatz mit
        //    der echten Altsystem-Nummer, und neu vergebene Nummern schliessen an die vorhandenen an.
        List<AufbereiteterAltkunde> reihenfolge = new ArrayList<>(zeilen);
        reihenfolge.sort(Comparator.comparing(AufbereiteterAltkunde::ohneKundennummer));

        int importiert = 0;
        int bereitsVorhanden = 0;
        List<Zeilenmeldung> dubletten = new ArrayList<>();
        List<Zeilenmeldung> fehler = new ArrayList<>();
        List<Zeilenmeldung> hinweise = new ArrayList<>();

        for (AufbereiteterAltkunde zeile : reihenfolge) {
            if (zeile.hatFehler()) {
                fehler.add(meldung(zeile, kundennummer(zeile), String.join("; ", zeile.fehler())));
                continue;
            }
            KundeRequest request = zeile.request();
            String verstoesse = pruefe(request);
            if (verstoesse != null) {
                fehler.add(meldung(zeile, request.kundennummer(), verstoesse));
                continue;
            }
            switch (abgleich.pruefe(request)) {
                case KundenAbgleich.BereitsVorhanden _ -> bereitsVorhanden++;
                // Zusatzangaben der Dublette (z. B. eine weitere Handynummer) mit ausgeben - sie gehen sonst verloren
                case KundenAbgleich.Dublette dublette -> dubletten.add(meldung(zeile, request.kundennummer(),
                        dublette.meldung() + ". Angaben dieser Zeile: " + kontaktangaben(request)));
                case KundenAbgleich.Konflikt konflikt -> fehler.add(meldung(zeile, request.kundennummer(), konflikt.meldung()));
                case KundenAbgleich.Neu _ -> {
                    List<String> zeilenHinweise = new ArrayList<>(zeile.hinweise());
                    if (request.kundennummer() == null) {
                        String nummer = abgleich.naechsteKundennummer();
                        request = request.mitKundennummer(nummer);
                        zeilenHinweise.addFirst("Keine Kundennummer im Altsystem – neue Nummer " + nummer + " vergeben");
                    }
                    try {
                        var angelegt = kundeService.anlegen(request, AuditAktion.IMPORTIERT);
                        abgleich.merke(angelegt);
                        importiert++;
                        if (!zeilenHinweise.isEmpty()) {
                            hinweise.add(meldung(zeile, angelegt.kundennummer(), String.join("; ", zeilenHinweise)));
                        }
                    } catch (ResponseStatusException e) {
                        fehler.add(meldung(zeile, request.kundennummer(), e.getReason()));
                    }
                }
            }
        }

        // Protokoll auch ins Server-Log (nachvollziehbar, wer wann was importiert hat)
        fehler.forEach(f -> LOG.warn("Kunden-Import Zeile {} nicht übernommen: {}", f.zeile(), f.meldung()));
        dubletten.forEach(d -> LOG.info("Kunden-Import Zeile {}: {}", d.zeile(), d.meldung()));
        LOG.info("Kunden-Import fertig: {} importiert, {} bereits vorhanden, {} Dubletten, {} Fehler, {} mit Hinweisen",
                importiert, bereitsVorhanden, dubletten.size(), fehler.size(), hinweise.size());

        return new KundenImportErgebnis(importiert, bereitsVorhanden, sortiert(dubletten), sortiert(fehler),
                sortiert(hinweise));
    }

    /** Bean Validation (Laenge, PLZ-Format, E-Mail ...) - Zeilen ohne Nummer mit Platzhalter pruefen */
    private String pruefe(KundeRequest request) {
        var zuPruefen = request.kundennummer() == null ? request.mitKundennummer("K-0000") : request;
        var verstoesse = validator.validate(zuPruefen);
        return verstoesse.isEmpty() ? null
                : verstoesse.stream().map(ConstraintViolation::getMessage).sorted().collect(Collectors.joining("; "));
    }

    // ---------------------------------------------------------------- Excel lesen

    private Sheet findeKundenblatt(Workbook workbook) {
        for (Sheet blatt : workbook) {
            if (leseKopfzeile(blatt.getRow(0)).keySet().containsAll(PFLICHTSPALTEN)) {
                return blatt;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kein Tabellenblatt mit Kundendaten gefunden – "
                + "erwartet werden in Zeile 1 die Spalten KdNr, Kunde / Ansprechpartner und Adresse");
    }

    private Map<String, Integer> leseKopfzeile(Row kopf) {
        Map<String, Integer> spalten = new HashMap<>();
        if (kopf == null) {
            return spalten;
        }
        for (Cell cell : kopf) {
            String ueberschrift = vereinfache(formatter.formatCellValue(cell));
            UEBERSCHRIFTEN.forEach((feld, namen) -> {
                if (namen.contains(ueberschrift)) {
                    spalten.putIfAbsent(feld, cell.getColumnIndex());
                }
            });
        }
        return spalten;
    }

    private AltkundeRoh leseZeile(Row row, int zeile, Map<String, Integer> spalten) {
        return new AltkundeRoh(zeile,
                text(row, spalten.get("kundennummer")),
                text(row, spalten.get("name")),
                text(row, spalten.get("adresse")),
                text(row, spalten.get("telefon")),
                text(row, spalten.get("email")),
                text(row, spalten.get("kundentyp")),
                text(row, spalten.get("kundeseit")),
                text(row, spalten.get("anmerkung")));
    }

    /** Zellinhalt als Text; echte Excel-Datumszellen als "21.06.2016" */
    private String text(Row row, Integer spalte) {
        if (spalte == null) {
            return null;
        }
        Cell cell = row.getCell(spalte);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().format(DEUTSCHES_DATUM);
        }
        String wert = formatter.formatCellValue(cell).trim();
        return wert.isEmpty() ? null : wert;
    }

    private boolean istLeer(Row row) {
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- Hilfsmethoden

    private static String kundennummer(AufbereiteterAltkunde zeile) {
        return zeile.request() != null ? zeile.request().kundennummer() : zeile.kundennummerRoh();
    }

    /** "Tel. 0160 5227787; E-Mail info@...; Weitere Telefonnummern: ..." - zum Nachtragen beim vorhandenen Kunden */
    private static String kontaktangaben(KundeRequest request) {
        List<String> angaben = new ArrayList<>();
        if (request.telefon() != null) {
            angaben.add("Tel. " + request.telefon());
        }
        if (request.email() != null) {
            angaben.add("E-Mail " + request.email());
        }
        if (request.bemerkung() != null) {
            angaben.add(request.bemerkung());
        }
        return angaben.isEmpty() ? "keine Kontaktangaben" : String.join("; ", angaben);
    }

    private static Zeilenmeldung meldung(AufbereiteterAltkunde zeile, String kundennummer, String text) {
        return new Zeilenmeldung(zeile.zeile(), kundennummer, text);
    }

    private static List<Zeilenmeldung> sortiert(List<Zeilenmeldung> meldungen) {
        return meldungen.stream().sorted(Comparator.comparingInt(Zeilenmeldung::zeile)).toList();
    }

    private static String vereinfache(String ueberschrift) {
        return ueberschrift.toLowerCase(Locale.GERMAN)
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss")
                .replaceAll("[^a-z0-9]", "");
    }
}
