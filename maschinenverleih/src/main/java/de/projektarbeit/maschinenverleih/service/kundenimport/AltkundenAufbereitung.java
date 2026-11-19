package de.projektarbeit.maschinenverleih.service.kundenimport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.projektarbeit.maschinenverleih.dto.KundeRequest;
import de.projektarbeit.maschinenverleih.model.Anrede;
import de.projektarbeit.maschinenverleih.model.Kundentyp;

/**
 * Bereitet eine Kundenzeile aus dem alten Excel-System fuer das neue Datenmodell auf.
 *
 * Im Altsystem stehen mehrere Angaben in einer Zelle: Firma und Ansprechpartner ("GaLaBau Krause GmbH /
 * Fr. Krause"), Strasse, PLZ und Ort in wechselnder Reihenfolge, mehrere Telefonnummern und E-Mails.
 * Diese Klasse zerlegt sie in die Felder der Tabelle kunde und bereinigt bekannte Schreibfehler.
 * - Jede inhaltliche Korrektur wird als Hinweis notiert (nichts wird stillschweigend geaendert).
 * - Was sich nicht eindeutig zuordnen laesst, wird als Fehler gemeldet statt geraten.
 * - Zusaetzliche Telefonnummern/E-Mails und alte Anmerkungen landen in der Bemerkung (nichts geht verloren).
 *
 * Reine Logik ohne Datenbank - deshalb mit Unit-Tests pruefbar (AltkundenAufbereitungTest).
 */
public class AltkundenAufbereitung {

    /** Rechtsformen - der Teil des Namens mit Rechtsform ist die Firma, der andere der Ansprechpartner. */
    private static final Pattern RECHTSFORM = Pattern.compile(
            "(?<!\\p{L})(GmbH|Gmbh|GMBH|G\\.m\\.b\\.H\\.|UG|GbR|OHG|KG|AG|e\\.\\s?K\\.|eK)(?!\\p{L})");

    /** "Firma (Herr Klein)" */
    private static final Pattern KLAMMER = Pattern.compile("^(.+?)\\s*\\((.+)\\)$");

    /** Trenner zwischen Firma und Ansprechpartner: " - z. Hd. ", " / ", "; ", ", " */
    private static final Pattern TRENNER = Pattern.compile("\\s+-\\s+(?:z\\.\\s*Hd\\.\\s*)?|\\s*/\\s*|\\s*;\\s*|,\\s+");

    /** Vorsaetze vor dem Namen des Ansprechpartners; Gruppe 1 = die Anrede selbst */
    private static final Pattern ANREDE = Pattern.compile(
            "^(?:(?:Ansprechpartner(?:in)?:|z\\.\\s*Hd\\.)\\s*)*(?:(Herrn?|Hr\\.?|Frau|Fr\\.?)\\s+)?",
            Pattern.CASE_INSENSITIVE);

    /** "42859 Remscheid, Nordring 25" */
    private static final Pattern PLZ_VORNE = Pattern.compile("^(\\d{5})\\s+([^,;]+?)\\s*[,;]\\s*(.+)$");

    /** "Am Hang 36, 42651 Solingen" oder "Talstrasse 2 42651 Solingen" */
    private static final Pattern PLZ_HINTEN = Pattern.compile("^(.+?)[\\s,;]+(\\d{5})\\s+(.+)$");

    private static final Pattern HAUSNUMMER = Pattern.compile("\\d+\\s*[a-zA-Z]?$");

    /** Eine Telefonnummer: Vorwahl, optional " / " oder "-", dann die Rufnummer (auch mit Leerzeichen) */
    private static final Pattern TELEFON = Pattern.compile(
            "(?:\\+49\\s?|0)\\d{2,5}(?:\\s*[/-]\\s*|\\s)?\\d(?:[\\d ]*\\d)?");

    /** Zerlegt eine Nummer mit erkennbarer Vorwahl, z. B. "02574 / 35 93 99" oder "0171-2556017" */
    private static final Pattern TELEFON_TEILE = Pattern.compile("^(\\+49\\s?|0)(\\d{2,5})(?:\\s*[/-]\\s*|\\s)(.+)$");

    private static final DateTimeFormatter DEUTSCHES_DATUM =
            DateTimeFormatter.ofPattern("d.M.uuuu").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Schreibvarianten aus dem Altsystem -> richtiger Ortsname. Bewusst eine feste, nachvollziehbare
     * Tabelle: Unbekannte Orte werden nicht "geraten", sondern so uebernommen, wie sie dastehen.
     */
    private static final Map<String, String> ORT_KORREKTUREN = Map.of(
            "sollingen", "Solingen",
            "sg", "Solingen",
            "remschied", "Remscheid",
            "remscheidt", "Remscheid",
            "w-tal", "Wuppertal",
            "wupertal", "Wuppertal",
            "leverkusn", "Leverkusen");

    private static final Map<String, Kundentyp> KUNDENTYPEN = Map.ofEntries(
            Map.entry("privat", Kundentyp.PRIVAT),
            Map.entry("privatkunde", Kundentyp.PRIVAT),
            Map.entry("privatperson", Kundentyp.PRIVAT),
            Map.entry("pk", Kundentyp.PRIVAT),
            Map.entry("gewerbe", Kundentyp.GEWERBE),
            Map.entry("gewerbekunde", Kundentyp.GEWERBE),
            Map.entry("gewerblich", Kundentyp.GEWERBE),
            Map.entry("firma", Kundentyp.GEWERBE),
            Map.entry("firmenkunde", Kundentyp.GEWERBE));

    record Name(String firma, Anrede anrede, String vorname, String nachname) {
    }

    /** Person ohne Vorsaetze, z. B. "Frau Krause" -> (FRAU, "Krause") */
    private record Person(Anrede anrede, String name) {
    }

    record Anschrift(String strasse, String plz, String ort) {
    }

    public AufbereiteterAltkunde bereiteAuf(AltkundeRoh roh) {
        List<String> hinweise = new ArrayList<>();
        List<String> fehler = new ArrayList<>();
        List<String> bemerkungen = new ArrayList<>();

        String kundennummer = kundennummer(roh.kundennummer(), fehler);
        Name name = name(roh.name(), fehler);
        Kundentyp typ = kundentyp(roh.kundentyp(), name, hinweise, fehler);
        Anschrift anschrift = anschrift(roh.adresse(), hinweise, fehler);
        String telefon = telefon(roh.telefon(), bemerkungen, hinweise);
        String email = email(roh.email(), bemerkungen, hinweise);
        LocalDate kundeSeit = kundeSeit(roh.kundeSeit(), hinweise);
        anmerkung(roh.anmerkung(), bemerkungen);

        if (!fehler.isEmpty()) {
            return new AufbereiteterAltkunde(roh.zeile(), roh.kundennummer(), null, List.copyOf(hinweise),
                    List.copyOf(fehler));
        }
        var request = new KundeRequest(kundennummer, typ, name.firma(), name.anrede(), name.vorname(), name.nachname(), email,
                telefon, anschrift.strasse(), anschrift.plz(), anschrift.ort(), null, kundeSeit,
                bemerkungen.isEmpty() ? null : String.join("; ", bemerkungen));
        return new AufbereiteterAltkunde(roh.zeile(), roh.kundennummer(), request, List.copyOf(hinweise), List.of());
    }

    // ---------------------------------------------------------------- Kundennummer

    private String kundennummer(String roh, List<String> fehler) {
        String text = einzeilig(roh);
        if (text == null) {
            return null; // fehlt im Altsystem -> der Import vergibt eine neue Nummer
        }
        String nummer = text.toUpperCase(Locale.ROOT).replaceAll("\\s", "");
        if (!nummer.matches("K-\\d{4,}")) {
            fehler.add("Kundennummer '" + text + "' hat nicht das Format K-1234");
        }
        return nummer;
    }

    // ---------------------------------------------------------------- Name / Firma / Ansprechpartner

    Name name(String roh, List<String> fehler) {
        String text = einzeilig(roh);
        if (text == null) {
            fehler.add("Name fehlt");
            return null;
        }
        String hauptteil = text;
        String zweiterTeil = null;
        Matcher klammer = KLAMMER.matcher(text);
        if (klammer.matches()) {
            hauptteil = klammer.group(1).trim();
            zweiterTeil = klammer.group(2).trim();
        } else {
            Matcher trenner = TRENNER.matcher(text);
            if (trenner.find()) {
                String links = text.substring(0, trenner.start()).trim();
                String rechts = text.substring(trenner.end()).trim();
                // Die Firma kann auch hinten stehen: "Frau Weber, Dachdecker Weber GmbH"
                boolean firmaRechts = !istFirma(links) && istFirma(rechts);
                hauptteil = firmaRechts ? rechts : links;
                zweiterTeil = firmaRechts ? links : rechts;
            }
        }

        if (istFirma(hauptteil)) {
            String firma = rechtsformVereinheitlichen(hauptteil);
            Person person = zweiterTeil == null ? new Person(null, "") : ohneAnrede(zweiterTeil);
            if (person.name().isEmpty()) {
                fehler.add("Ansprechpartner fehlt bei der Firma '" + firma + "'");
                return null;
            }
            // "Krause" -> nur Nachname; "Erika Krause" -> Vor- und Nachname
            return personenName(firma, person);
        }

        // Privatperson: "Birgit Krause" oder "Krause, Birgit" (ggf. mit "Frau"/"Herr" davor)
        Person person;
        if (zweiterTeil == null) {
            person = ohneAnrede(hauptteil);
        } else if (text.contains(",") && !text.contains("/")) {
            Person vorne = ohneAnrede(zweiterTeil);
            Person hinten = ohneAnrede(hauptteil);
            person = new Person(vorne.anrede() != null ? vorne.anrede() : hinten.anrede(),
                    vorne.name() + " " + hinten.name());
        } else {
            fehler.add("Name nicht eindeutig zerlegbar (keine Firma mit Rechtsform erkennbar): '" + text + "'");
            return null;
        }
        if (person.name().split("\\s+").length < 2) {
            fehler.add("Vor- und Nachname nicht erkennbar: '" + text + "'");
            return null;
        }
        return personenName(null, person);
    }

    private static Name personenName(String firma, Person person) {
        String[] teile = person.name().split("\\s+");
        String nachname = teile[teile.length - 1];
        String vorname = teile.length > 1 ? String.join(" ", Arrays.copyOf(teile, teile.length - 1)) : null;
        return new Name(firma, person.anrede(), vorname, nachname);
    }

    static boolean istFirma(String text) {
        return RECHTSFORM.matcher(text).find();
    }

    /** "Gmbh" / "G.m.b.H." -> "GmbH", "eK" -> "e.K." (nur Schreibweise, kein inhaltlicher Eingriff) */
    private static String rechtsformVereinheitlichen(String firma) {
        return firma
                .replaceAll("(?<!\\p{L})(?:Gmbh|GMBH|G\\.m\\.b\\.H\\.)(?!\\p{L})", "GmbH")
                .replaceAll("(?<!\\p{L})eK(?!\\p{L})", "e.K.")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Entfernt Vorsaetze wie "z. Hd." und merkt sich die Anrede: "Herr", "Herrn", "Hr." -> HERR; "Frau", "Fr." -> FRAU.
     * Ohne Anrede bleibt sie leer - aus dem Vornamen wird bewusst nicht auf Frau/Herr geschlossen.
     */
    private static Person ohneAnrede(String text) {
        String bereinigt = text.trim();
        Matcher m = ANREDE.matcher(bereinigt);
        if (!m.lookingAt()) {
            return new Person(null, bereinigt);
        }
        Anrede anrede = m.group(1) == null ? null
                : m.group(1).toLowerCase(Locale.GERMAN).startsWith("h") ? Anrede.HERR : Anrede.FRAU;
        return new Person(anrede, bereinigt.substring(m.end()).trim());
    }

    // ---------------------------------------------------------------- Kundentyp

    private Kundentyp kundentyp(String roh, Name name, List<String> hinweise, List<String> fehler) {
        if (name == null) {
            return null;
        }
        Kundentyp amNamenErkannt = name.firma() != null ? Kundentyp.GEWERBE : Kundentyp.PRIVAT;
        String text = einzeilig(roh);
        Kundentyp angegeben = text == null ? null : KUNDENTYPEN.get(text.toLowerCase(Locale.GERMAN));
        if (angegeben == null) {
            hinweise.add((text == null ? "Kundentyp fehlt" : "Kundentyp '" + text + "' unbekannt")
                    + " – als " + amNamenErkannt.getBezeichnung() + " übernommen (am Namen erkannt)");
            return amNamenErkannt;
        }
        if (angegeben != amNamenErkannt) {
            fehler.add("Kundentyp '" + text + "' passt nicht zum Namen – "
                    + (angegeben == Kundentyp.GEWERBE ? "keine Firma mit Rechtsform erkennbar" : "Name enthält eine Firma"));
        }
        return angegeben;
    }

    // ---------------------------------------------------------------- Anschrift

    Anschrift anschrift(String roh, List<String> hinweise, List<String> fehler) {
        String text = roh == null ? null : einzeilig(roh.replace("\n", ", "));
        if (text == null) {
            fehler.add("Anschrift fehlt");
            return null;
        }
        String strasse;
        String plz;
        String ort;
        Matcher vorne = PLZ_VORNE.matcher(text);
        Matcher hinten = PLZ_HINTEN.matcher(text);
        if (vorne.matches()) {
            plz = vorne.group(1);
            ort = vorne.group(2);
            strasse = vorne.group(3);
        } else if (hinten.matches()) {
            strasse = hinten.group(1);
            plz = hinten.group(2);
            ort = hinten.group(3);
        } else {
            fehler.add("Anschrift unvollständig – PLZ und Ort fehlen: '" + text + "'");
            return null;
        }
        strasse = ohneTrennerAmRand(strasse);
        ort = ohneTrennerAmRand(ort);
        if (!HAUSNUMMER.matcher(strasse).find()) {
            fehler.add("Hausnummer fehlt in der Anschrift: '" + text + "'");
            return null;
        }
        return new Anschrift(strasse, plz, ort(ort, hinweise));
    }

    private static String ort(String ort, List<String> hinweise) {
        String korrigiert = ORT_KORREKTUREN.get(ort.toLowerCase(Locale.GERMAN));
        if (korrigiert != null) {
            hinweise.add("Ort '" + ort + "' korrigiert zu '" + korrigiert + "'");
            return korrigiert;
        }
        if (ort.length() > 3 && ort.equals(ort.toUpperCase(Locale.GERMAN))) {
            // "LEVERKUSEN" -> "Leverkusen" (nur Schreibweise)
            return ort.charAt(0) + ort.substring(1).toLowerCase(Locale.GERMAN);
        }
        return ort;
    }

    // ---------------------------------------------------------------- Telefon

    /** Erste Nummer -> Feld telefon; weitere Nummern und Fax -> Bemerkung. */
    String telefon(String roh, List<String> bemerkungen, List<String> hinweise) {
        if (roh == null || roh.isBlank()) {
            return null;
        }
        // Zeilenumbruch trennt Nummern - darf nicht zu einer Nummer verschmelzen
        String text = roh.replace("\n", "; ").replaceAll("[ \\t]+", " ").trim();
        String erste = null;
        List<String> weitere = new ArrayList<>();
        List<String> fax = new ArrayList<>();
        Matcher m = TELEFON.matcher(text);
        int ende = 0;
        while (m.find()) {
            String davor = text.substring(ende, m.start()).toLowerCase(Locale.GERMAN);
            String nummer = telefonVereinheitlichen(m.group());
            ende = m.end();
            if (davor.contains("fax")) {
                fax.add(nummer);
            } else if (erste == null) {
                erste = nummer;
            } else {
                String art = davor.contains("mobil") ? "mobil " : davor.contains("privat") ? "privat " : "";
                weitere.add(art + nummer);
            }
        }
        if (!weitere.isEmpty()) {
            bemerkungen.add("Weitere Telefonnummern: " + String.join(", ", weitere));
        }
        if (!fax.isEmpty()) {
            bemerkungen.add("Fax: " + String.join(", ", fax));
        }
        if (erste == null) {
            hinweise.add("Telefon '" + text + "' nicht erkennbar – nicht übernommen");
        }
        return erste;
    }

    /** "02574 / 35 93 99" -> "02574 359399", "0171-2556017" -> "0171 2556017" */
    private static String telefonVereinheitlichen(String nummer) {
        Matcher m = TELEFON_TEILE.matcher(nummer.trim());
        if (!m.matches()) {
            return nummer.replaceAll("\\s", ""); // ohne Trenner ist die Vorwahl nicht sicher erkennbar
        }
        String vorsatz = m.group(1).trim().equals("+49") ? "+49 " : "0";
        return vorsatz + m.group(2) + " " + m.group(3).replaceAll("\\s", "");
    }

    // ---------------------------------------------------------------- E-Mail

    /** Erste Adresse -> Feld email; weitere Adressen -> Bemerkung. */
    String email(String roh, List<String> bemerkungen, List<String> hinweise) {
        if (roh == null || roh.isBlank()) {
            return null;
        }
        List<String> adressen = Arrays.stream(roh.split("[\\s,;/]+"))
                .map(String::trim)
                .filter(a -> a.contains("@"))
                .map(a -> a.toLowerCase(Locale.ROOT))
                .toList();
        if (adressen.isEmpty()) {
            hinweise.add("E-Mail '" + einzeilig(roh) + "' nicht erkennbar – nicht übernommen");
            return null;
        }
        if (adressen.size() > 1) {
            bemerkungen.add("Weitere E-Mail-Adressen: " + String.join(", ", adressen.subList(1, adressen.size())));
        }
        String email = adressen.getFirst();
        if (!email.matches("\\p{ASCII}+")) {
            hinweise.add("E-Mail '" + email + "' enthält Umlaute – bitte prüfen, ob sie zustellbar ist");
        }
        return email;
    }

    // ---------------------------------------------------------------- Kunde seit / Anmerkung

    private LocalDate kundeSeit(String roh, List<String> hinweise) {
        String text = einzeilig(roh);
        if (text == null) {
            return null;
        }
        if (text.matches("\\d{4}")) {
            hinweise.add("Kunde seit: nur das Jahr " + text + " angegeben – als 01.01." + text + " übernommen");
            return LocalDate.of(Integer.parseInt(text), 1, 1);
        }
        try {
            return text.contains(".") ? LocalDate.parse(text, DEUTSCHES_DATUM) : LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            hinweise.add("Kunde seit '" + text + "' ist kein gültiges Datum – leer gelassen");
            return null;
        }
    }

    /** Fachliche Anmerkungen ("zahlt oft verspätet") bleiben erhalten; Notizen zum Import selbst nicht. */
    private void anmerkung(String roh, List<String> bemerkungen) {
        String text = einzeilig(roh);
        if (text == null) {
            return;
        }
        List<String> teile = Arrays.stream(text.split(";"))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .filter(t -> !t.toLowerCase(Locale.GERMAN).matches(".*(kdnr fehlt|dublette).*"))
                .toList();
        if (!teile.isEmpty()) {
            bemerkungen.add("Altsystem: " + String.join("; ", teile));
        }
    }

    // ---------------------------------------------------------------- Hilfsmethoden

    /** Leerraum vereinheitlichen; leerer Text -> null */
    private static String einzeilig(String text) {
        if (text == null) {
            return null;
        }
        String ergebnis = text.replaceAll("\\s+", " ").trim();
        return ergebnis.isEmpty() ? null : ergebnis;
    }

    private static String ohneTrennerAmRand(String text) {
        return text.replaceAll("^[\\s,;]+|[\\s,;]+$", "");
    }
}
