package de.projektarbeit.maschinenverleih.service.kundenimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.projektarbeit.maschinenverleih.dto.KundeRequest;
import de.projektarbeit.maschinenverleih.dto.KundeResponse;

/**
 * Entscheidet fuer jeden aufbereiteten Altkunden: neu anlegen, schon vorhanden, Dublette oder Konflikt.
 *
 * Er kennt alle Kunden aus der Datenbank und merkt sich jeden, der in diesem Lauf angelegt wird.
 * Dadurch werden Dubletten innerhalb der Datei genauso erkannt wie bei einem zweiten Importlauf -
 * der Import ist wiederholbar, ohne doppelte Kunden zu erzeugen. Reine Logik ohne Datenbank.
 *
 * Regeln:
 *  1. Kundennummer schon vergeben und es ist derselbe Kunde (gleicher Name oder gleiche E-Mail) -> bereits vorhanden
 *     Kundennummer schon vergeben, aber an jemand anderen                                     -> Konflikt
 *  2. Gleiche Anschrift UND (gleiche E-Mail ODER gleicher Name) wie ein bekannter Kunde        -> Dublette
 *  3. Gleiche E-Mail, aber andere Anschrift: unklar, ob derselbe Kunde                       -> Konflikt (klaeren)
 *  4. sonst                                                                                   -> neu anlegen
 */
public class KundenAbgleich {

    public sealed interface Entscheidung permits Neu, BereitsVorhanden, Dublette, Konflikt {
    }

    public record Neu() implements Entscheidung {
    }

    public record BereitsVorhanden(String kundennummer) implements Entscheidung {
    }

    public record Dublette(String meldung) implements Entscheidung {
    }

    public record Konflikt(String meldung) implements Entscheidung {
    }

    /** Ein bekannter Kunde mit seinen Vergleichsschluesseln (vereinfachte Schreibweise, siehe unten). */
    private record Bekannt(String kundennummer, String anzeigename, String email, String name, String adresse) {
    }

    private static final Pattern NUMMER = Pattern.compile("K-(\\d+)");

    private final List<Bekannt> bekannte = new ArrayList<>();
    private final Set<Integer> belegteNummern = new HashSet<>();

    public KundenAbgleich(List<KundeResponse> vorhandeneKunden) {
        vorhandeneKunden.forEach(this::merke);
    }

    /**
     * Nummern aus der Importdatei gelten als belegt - auch die von Zeilen, die (noch) nicht importiert werden
     * konnten. Sonst koennte eine neu vergebene Nummer spaeter mit einer korrigierten Altdaten-Zeile kollidieren.
     */
    public void reserviere(Collection<String> kundennummern) {
        kundennummern.forEach(this::belege);
    }

    public Entscheidung pruefe(KundeRequest kunde) {
        String email = emailSchluessel(kunde.email());
        String name = nameSchluessel(kunde.firma(), kunde.vorname(), kunde.nachname());
        String adresse = adressSchluessel(kunde.strasse(), kunde.plz());

        if (kunde.kundennummer() != null) {
            Optional<Bekannt> gleicheNummer = bekannte.stream()
                    .filter(b -> b.kundennummer().equalsIgnoreCase(kunde.kundennummer()))
                    .findFirst();
            if (gleicheNummer.isPresent()) {
                Bekannt b = gleicheNummer.get();
                boolean derselbe = b.name().equals(name) || (email != null && email.equals(b.email()));
                return derselbe
                        ? new BereitsVorhanden(b.kundennummer())
                        : new Konflikt("Kundennummer " + kunde.kundennummer() + " ist bereits an „"
                                + b.anzeigename() + "“ vergeben");
            }
        }

        for (Bekannt b : bekannte) {
            boolean gleicheAdresse = adresse.equals(b.adresse());
            boolean gleicheEmail = email != null && email.equals(b.email());
            boolean gleicherName = name.equals(b.name());
            if (gleicheAdresse && (gleicheEmail || gleicherName)) {
                String grund = (gleicherName ? "gleicher Name, " : "") + (gleicheEmail ? "gleiche E-Mail, " : "")
                        + "gleiche Anschrift";
                String anfang = kunde.kundennummer() == null
                        ? "Ohne Kundennummer im Altsystem – entspricht dem Kunden "
                        : "Vermutlich Dublette von ";
                return new Dublette(anfang + b.kundennummer() + " (" + grund + "), nicht angelegt");
            }
        }

        for (Bekannt b : bekannte) {
            if (email != null && email.equals(b.email())) {
                return new Konflikt("E-Mail " + kunde.email() + " gehört bereits zu " + b.kundennummer() + " („"
                        + b.anzeigename() + "“, andere Anschrift) – bitte klären, ob es derselbe Kunde ist");
            }
        }
        return new Neu();
    }

    /** Nach dem Speichern aufrufen, damit spaetere Zeilen mit diesem Kunden verglichen werden. */
    public void merke(KundeResponse kunde) {
        bekannte.add(new Bekannt(kunde.kundennummer(), kunde.anzeigename(), emailSchluessel(kunde.email()),
                nameSchluessel(kunde.firma(), kunde.vorname(), kunde.nachname()),
                adressSchluessel(kunde.strasse(), kunde.plz())));
        belege(kunde.kundennummer());
    }

    /** Hoechste belegte Nummer + 1, z. B. K-1119 */
    public String naechsteKundennummer() {
        int hoechste = belegteNummern.stream().mapToInt(Integer::intValue).max().orElse(1000);
        return String.format("K-%04d", hoechste + 1);
    }

    private void belege(String kundennummer) {
        if (kundennummer == null) {
            return;
        }
        Matcher m = NUMMER.matcher(kundennummer.trim().toUpperCase(Locale.ROOT));
        if (m.matches()) {
            belegteNummern.add(Integer.parseInt(m.group(1)));
        }
    }

    // ---------------------------------------------------------------- Vergleichsschluessel
    // Damit "Kanalbau Krueger GbR, Hauptstr. 17" und "Kanalbau Krüger GbR, Hauptstraße 17" als gleich gelten.

    static String emailSchluessel(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    static String nameSchluessel(String firma, String vorname, String nachname) {
        String name = firma != null ? firma : (vorname == null ? "" : vorname + " ") + nachname;
        String ohneRechtsform = vereinfache(name)
                .replaceAll("\\b(gmbh|g\\.m\\.b\\.h\\.|ug|gbr|ohg|kg|ag|e\\.\\s?k\\.|ek|co\\.?)(?=\\W|$)", " ");
        return nurBuchstabenUndZiffern(ohneRechtsform);
    }

    static String adressSchluessel(String strasse, String plz) {
        return nurBuchstabenUndZiffern(vereinfache(strasse).replace("strasse", "str")) + "|" + plz;
    }

    private static String vereinfache(String text) {
        return text.toLowerCase(Locale.GERMAN)
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
    }

    private static String nurBuchstabenUndZiffern(String text) {
        return text.replaceAll("[^a-z0-9]", "");
    }
}
