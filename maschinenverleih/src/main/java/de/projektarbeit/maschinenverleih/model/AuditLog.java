package de.projektarbeit.maschinenverleih.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Ein Eintrag wird nur angelegt, nie geaendert - deshalb gibt es keine Setter.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime zeitpunkt;
    private String benutzer;

    @Enumerated(EnumType.STRING)
    private AuditAktion aktion;

    private String tabelle;
    private Long datensatzId;
    private String alteWerte;
    private String neueWerte;

    protected AuditLog() {
    }

    public AuditLog(String benutzer, AuditAktion aktion, String tabelle, Long datensatzId,
            String alteWerte, String neueWerte) {
        this.zeitpunkt = LocalDateTime.now();
        this.benutzer = benutzer;
        this.aktion = aktion;
        this.tabelle = tabelle;
        this.datensatzId = datensatzId;
        this.alteWerte = alteWerte;
        this.neueWerte = neueWerte;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getZeitpunkt() {
        return zeitpunkt;
    }

    public String getBenutzer() {
        return benutzer;
    }

    public AuditAktion getAktion() {
        return aktion;
    }

    public String getTabelle() {
        return tabelle;
    }

    public Long getDatensatzId() {
        return datensatzId;
    }

    public String getAlteWerte() {
        return alteWerte;
    }

    public String getNeueWerte() {
        return neueWerte;
    }
}
