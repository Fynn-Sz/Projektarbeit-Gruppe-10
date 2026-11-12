package de.projektarbeit.maschinenverleih.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.projektarbeit.maschinenverleih.dto.AuditLogResponse;
import de.projektarbeit.maschinenverleih.model.AuditAktion;
import de.projektarbeit.maschinenverleih.model.MaschinenStatus;

/**
 * Bereitet einen Audit-Eintrag fuer die Historienseite auf: aus den gespeicherten Texten
 * (z. B. "MaschineResponse[id=1, baujahr=2019, ...]") wird eine Liste "Feld | vorher | nachher".
 */
public record AuditAnzeige(LocalDateTime zeitpunkt, String benutzer, AuditAktion aktion, List<Feld> felder) {

    public record Feld(String name, String vorher, String nachher) {
    }

    // Reihenfolge und Anzeigenamen der Felder; IDs werden nicht angezeigt
    private static final Map<String, String> NAMEN = new LinkedHashMap<>();

    static {
        NAMEN.put("inventarnummer", "Inventarnummer");
        NAMEN.put("bezeichnung", "Bezeichnung");
        NAMEN.put("kategorie", "Kategorie");
        NAMEN.put("standort", "Standort");
        NAMEN.put("hersteller", "Hersteller");
        NAMEN.put("baujahr", "Baujahr");
        NAMEN.put("betriebsstunden", "Betriebsstunden");
        NAMEN.put("wartungsintervallStd", "Wartungsintervall (Std.)");
        NAMEN.put("tagesmietpreis", "Tagesmietpreis (€)");
        NAMEN.put("garantieBis", "Garantie bis");
        NAMEN.put("status", "Status");
    }

    public static AuditAnzeige von(AuditLogResponse log) {
        Map<String, String> alt = zerlege(log.alteWerte());
        Map<String, String> neu = zerlege(log.neueWerte());
        List<Feld> felder = new ArrayList<>();
        NAMEN.forEach((schluessel, name) -> {
            String vorher = alt.get(schluessel);
            String nachher = neu.get(schluessel);
            // Bei Aenderungen nur die Felder zeigen, die sich wirklich geaendert haben
            if (log.aktion() == AuditAktion.GEAENDERT && Objects.equals(vorher, nachher)) {
                return;
            }
            if (vorher != null || nachher != null) {
                felder.add(new Feld(name, lesbar(schluessel, vorher), lesbar(schluessel, nachher)));
            }
        });
        return new AuditAnzeige(log.zeitpunkt(), log.benutzer(), log.aktion(), felder);
    }

    // Gespeicherte Werte so anzeigen, wie sie auch sonst auf der Seite stehen
    private static String lesbar(String schluessel, String wert) {
        if (wert == null) {
            return null;
        }
        try {
            return switch (schluessel) {
                case "status" -> MaschinenStatus.valueOf(wert).getBezeichnung();
                case "garantieBis" -> LocalDate.parse(wert).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                case "tagesmietpreis" -> wert.replace('.', ',');
                default -> wert;
            };
        } catch (RuntimeException e) {
            return wert;
        }
    }

    static Map<String, String> zerlege(String text) {
        Map<String, String> werte = new LinkedHashMap<>();
        if (text == null || !text.contains("[")) {
            return werte;
        }
        String inhalt = text.substring(text.indexOf('[') + 1, text.lastIndexOf(']'));
        // Trennen an ", schluessel=" - so bleiben Kommas innerhalb eines Wertes erhalten
        for (String teil : inhalt.split(", (?=\\w+=)")) {
            int gleich = teil.indexOf('=');
            if (gleich > 0) {
                String wert = teil.substring(gleich + 1);
                werte.put(teil.substring(0, gleich), "null".equals(wert) ? null : wert);
            }
        }
        return werte;
    }
}
