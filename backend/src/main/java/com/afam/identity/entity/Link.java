package com.afam.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "link")
public class Link {

    @Id
    @Column(name = "identificatore_link", length = 255)
    private String identificatoreLink;

    @Column(name = "data_scadenza")
    protected LocalDateTime dataScadenza;

    @Column(name = "risorsa_associata", length = 255)
    private String risorsaAssociata;

    @Column(length = 50)
    private String stato;

    @Column(columnDefinition = "TEXT")
    private String impostazioni;

    @Column(name = "numero_visualizzazioni")
    protected Integer numeroVisualizzazioni = 0;

    @ManyToOne
    @JoinColumn(name = "profilo_id")
    private Profilo profilo;

    @ManyToOne
    @JoinColumn(name = "gruppo_id")
    private Gruppo gruppo;

    @ManyToOne
    @JoinColumn(name = "contenuto_id")
    private Contenuto contenuto;

    // Metodi UML (segnaposto per aderenza 100% al RAD)
    public void generaLink() {}
    public void disabilitaLink() {}
    public void modificaImpostazioni() {}
    public boolean checkLink() { return false; }
    public boolean isAttivo() { return false; }
    public void incrementaVisualizzazioni() {}

    public String getIdentificatoreLink() { return identificatoreLink; }
    public void setIdentificatoreLink(String identificatoreLink) { this.identificatoreLink = identificatoreLink; }
    public LocalDateTime getDataScadenza() { return dataScadenza; }
    public void setDataScadenza(LocalDateTime dataScadenza) { this.dataScadenza = dataScadenza; }
    public String getRisorsaAssociata() { return risorsaAssociata; }
    public void setRisorsaAssociata(String risorsaAssociata) { this.risorsaAssociata = risorsaAssociata; }
    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }
    public String getImpostazioni() { return impostazioni; }
    public void setImpostazioni(String impostazioni) { this.impostazioni = impostazioni; }
    public Integer getNumeroVisualizzazioni() { return numeroVisualizzazioni; }
    public void setNumeroVisualizzazioni(Integer numeroVisualizzazioni) { this.numeroVisualizzazioni = numeroVisualizzazioni; }
    public Profilo getProfilo() { return profilo; }
    public void setProfilo(Profilo profilo) { this.profilo = profilo; }
    public Gruppo getGruppo() { return gruppo; }
    public void setGruppo(Gruppo gruppo) { this.gruppo = gruppo; }
    public Contenuto getContenuto() { return contenuto; }
    public void setContenuto(Contenuto contenuto) { this.contenuto = contenuto; }
}
