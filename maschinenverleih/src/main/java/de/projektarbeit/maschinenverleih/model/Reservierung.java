package de.projektarbeit.maschinenverleih.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservierung")
public class Reservierung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kunde_id")
    private Kunde kunde;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maschine_id")
    private Maschine maschine;

    private LocalDate von;
    private LocalDate bis;

    @Enumerated(EnumType.STRING)
    private ReservierungsStatus status;

    private LocalDateTime erstelltAm;

    protected Reservierung() {
    }

    public Reservierung(Kunde kunde, Maschine maschine, LocalDate von, LocalDate bis, ReservierungsStatus status) {
        this.kunde = kunde;
        this.maschine = maschine;
        this.von = von;
        this.bis = bis;
        this.status = status;
    }

    @PrePersist
    void vorDemSpeichern() {
        erstelltAm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Kunde getKunde() {
        return kunde;
    }

    public Maschine getMaschine() {
        return maschine;
    }

    public LocalDate getVon() {
        return von;
    }

    public LocalDate getBis() {
        return bis;
    }

    public ReservierungsStatus getStatus() {
        return status;
    }

    public void setStatus(ReservierungsStatus status) {
        this.status = status;
    }

    public LocalDateTime getErstelltAm() {
        return erstelltAm;
    }
}
