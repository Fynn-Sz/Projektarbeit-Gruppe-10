package de.projektarbeit.maschinenverleih.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import de.projektarbeit.maschinenverleih.dto.ImportErgebnis;
import de.projektarbeit.maschinenverleih.dto.ImportErgebnis.ImportFehler;
import de.projektarbeit.maschinenverleih.dto.MaschineRequest;
import de.projektarbeit.maschinenverleih.model.AuditAktion;
import de.projektarbeit.maschinenverleih.repository.KategorieRepository;
import de.projektarbeit.maschinenverleih.repository.StandortRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Liest Maschinen aus einer Excel-Datei (.xlsx). Erwartet in Zeile 1 die Spaltenueberschriften,
 * die Reihenfolge der Spalten ist egal. Jede Zeile wird einzeln geprueft und gespeichert:
 * fehlerhafte Zeilen landen im Fehlerbericht, bereits vorhandene Inventarnummern werden uebersprungen.
 */
@Service
public class MaschinenImportService {

    private static final List<String> PFLICHTSPALTEN = List.of("inventarnummer", "bezeichnung", "kategorie",
            "standort", "baujahr", "betriebsstunden", "wartungsintervall", "tagesmietpreis");
    private static final List<String> OPTIONALE_SPALTEN = List.of("hersteller", "garantiebis");
    private static final DateTimeFormatter DEUTSCHES_DATUM = DateTimeFormatter.ofPattern("d.M.yyyy");

    private final MaschineService maschineService;
    private final KategorieRepository kategorieRepository;
    private final StandortRepository standortRepository;
    private final Validator validator;
    private final DataFormatter formatter = new DataFormatter();

    public MaschinenImportService(MaschineService maschineService, KategorieRepository kategorieRepository,
            StandortRepository standortRepository, Validator validator) {
        this.maschineService = maschineService;
        this.kategorieRepository = kategorieRepository;
        this.standortRepository = standortRepository;
        this.validator = validator;
    }

    public ImportErgebnis importiere(MultipartFile datei) {
        if (datei == null || datei.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es wurde keine Datei hochgeladen");
        }
        try (Workbook workbook = WorkbookFactory.create(datei.getInputStream())) {
            return importiere(workbook.getSheetAt(0));
        } catch (IOException | EmptyFileException | UnsupportedFileFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Datei konnte nicht gelesen werden - bitte eine Excel-Datei (.xlsx) hochladen");
        }
    }

    private ImportErgebnis importiere(Sheet sheet) {
        Map<String, Integer> spalten = leseKopfzeile(sheet.getRow(0));
        int importiert = 0;
        int uebersprungen = 0;
        List<ImportFehler> fehler = new ArrayList<>();
        Set<String> inventarnummernInDatei = new HashSet<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (istLeer(row)) {
                continue;
            }
            int zeile = i + 1; // Zeilennummer wie in Excel angezeigt
            try {
                MaschineRequest request = leseZeile(row, spalten);
                Set<ConstraintViolation<MaschineRequest>> verstoesse = validator.validate(request);
                if (!verstoesse.isEmpty()) {
                    fehler.add(new ImportFehler(zeile, verstoesse.stream()
                            .map(ConstraintViolation::getMessage).sorted().collect(Collectors.joining("; "))));
                    continue;
                }
                String inventarnummer = request.inventarnummer().trim();
                if (!inventarnummernInDatei.add(inventarnummer) || maschineService.existiert(inventarnummer)) {
                    uebersprungen++;
                    continue;
                }
                maschineService.anlegen(request, AuditAktion.IMPORTIERT);
                importiert++;
            } catch (UngueltigerWertException e) {
                fehler.add(new ImportFehler(zeile, e.getMessage()));
            } catch (ResponseStatusException e) {
                fehler.add(new ImportFehler(zeile, e.getReason()));
            }
        }
        return new ImportErgebnis(importiert, uebersprungen, fehler);
    }

    private Map<String, Integer> leseKopfzeile(Row kopf) {
        if (kopf == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Die erste Zeile muss die Spaltenüberschriften enthalten");
        }
        Map<String, Integer> spalten = new HashMap<>();
        for (Cell cell : kopf) {
            String ueberschrift = normalisiere(formatter.formatCellValue(cell));
            for (String bekannt : alleSpalten()) {
                // "Wartungsintervall (Std)" oder "Tagesmietpreis in EUR" werden ebenfalls erkannt
                if (ueberschrift.startsWith(bekannt)) {
                    spalten.putIfAbsent(bekannt, cell.getColumnIndex());
                }
            }
        }
        List<String> fehlend = PFLICHTSPALTEN.stream().filter(s -> !spalten.containsKey(s)).toList();
        if (!fehlend.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pflichtspalten fehlen: " + String.join(", ", fehlend));
        }
        return spalten;
    }

    private MaschineRequest leseZeile(Row row, Map<String, Integer> spalten) {
        return new MaschineRequest(
                text(row, spalten.get("inventarnummer")),
                text(row, spalten.get("bezeichnung")),
                kategorieId(text(row, spalten.get("kategorie"))),
                standortId(text(row, spalten.get("standort"))),
                text(row, spalten.get("hersteller")),
                ganzzahl(row, spalten.get("baujahr"), "Baujahr"),
                ganzzahl(row, spalten.get("betriebsstunden"), "Betriebsstunden"),
                ganzzahl(row, spalten.get("wartungsintervall"), "Wartungsintervall"),
                zahl(row, spalten.get("tagesmietpreis"), "Tagesmietpreis"),
                datum(row, spalten.get("garantiebis"), "Garantie bis"),
                null);
    }

    private Long kategorieId(String name) {
        if (name == null) {
            return null;
        }
        return kategorieRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new UngueltigerWertException("Kategorie '" + name + "' ist unbekannt"))
                .getId();
    }

    private Long standortId(String name) {
        if (name == null) {
            return null;
        }
        return standortRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new UngueltigerWertException("Standort '" + name + "' ist unbekannt"))
                .getId();
    }

    private String text(Row row, Integer spalte) {
        Cell cell = zelle(row, spalte);
        if (cell == null) {
            return null;
        }
        String wert = formatter.formatCellValue(cell).trim();
        return wert.isEmpty() ? null : wert;
    }

    private BigDecimal zahl(Row row, Integer spalte, String feld) {
        Cell cell = zelle(row, spalte);
        if (cell == null) {
            return null;
        }
        if (typ(cell) == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        String wert = text(row, spalte);
        if (wert == null) {
            return null;
        }
        try {
            return new BigDecimal(wert.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new UngueltigerWertException(feld + " ist keine Zahl: '" + wert + "'");
        }
    }

    private Integer ganzzahl(Row row, Integer spalte, String feld) {
        BigDecimal wert = zahl(row, spalte, feld);
        if (wert == null) {
            return null;
        }
        try {
            return wert.intValueExact();
        } catch (ArithmeticException e) {
            throw new UngueltigerWertException(feld + " muss eine ganze Zahl sein: '" + wert.toPlainString() + "'");
        }
    }

    private LocalDate datum(Row row, Integer spalte, String feld) {
        Cell cell = zelle(row, spalte);
        if (cell == null) {
            return null;
        }
        if (typ(cell) == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String wert = text(row, spalte);
        if (wert == null) {
            return null;
        }
        try {
            return wert.contains(".") ? LocalDate.parse(wert, DEUTSCHES_DATUM) : LocalDate.parse(wert);
        } catch (DateTimeParseException e) {
            throw new UngueltigerWertException(feld + " ist kein gültiges Datum: '" + wert + "' (erwartet z. B. 31.12.2027)");
        }
    }

    private static Cell zelle(Row row, Integer spalte) {
        if (spalte == null) {
            return null;
        }
        Cell cell = row.getCell(spalte);
        return cell == null || cell.getCellType() == CellType.BLANK ? null : cell;
    }

    private static CellType typ(Cell cell) {
        return cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
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

    private static String normalisiere(String ueberschrift) {
        return ueberschrift.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static List<String> alleSpalten() {
        var alle = new ArrayList<>(PFLICHTSPALTEN);
        alle.addAll(OPTIONALE_SPALTEN);
        return alle;
    }

    private static class UngueltigerWertException extends RuntimeException {
        UngueltigerWertException(String meldung) {
            super(meldung);
        }
    }
}
