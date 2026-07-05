package com.afam.identity.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "contenuto")
public class Contenuto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(nullable = false)
    private String titolo;

    @Column(length = 50)
    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "policy_visibilita", length = 50)
    private String policyVisibilita;

    @Column(name = "numero_visualizzazioni")
    private Integer numeroVisualizzazioni = 0;

    @Column(name = "data_caricamento", nullable = false)
    private java.time.LocalDateTime dataCaricamento;

    @PrePersist
    protected void onCreate() {
        if (this.dataCaricamento == null) {
            this.dataCaricamento = java.time.LocalDateTime.now();
        }
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "profilo_id", nullable = false)
    private Profilo profilo;

    @OneToMany(mappedBy = "contenuto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Allegato> allegati;

    @ElementCollection
    @CollectionTable(name = "contenuto_autore", joinColumns = @JoinColumn(name = "contenuto_id"))
    @Column(name = "nome_autore")
    private List<String> autori;

    @ElementCollection
    @CollectionTable(name = "contenuto_collaboratore", joinColumns = @JoinColumn(name = "contenuto_id"))
    @Column(name = "nome_collaboratore")
    protected List<String> collaboratori;

    @JsonIgnore
    @OneToMany(mappedBy = "contenuto", cascade = CascadeType.ALL, orphanRemoval = true)
    protected List<Link> linkAssociati;

    @JsonIgnore
    @ManyToMany(mappedBy = "listaContenuti")
    private List<Gruppo> gruppi;

    // Metodi UML (segnaposto per aderenza 100% al RAD)
    public void caricaContenuto() {
    }

    public void modificaContenuto() {
    }

    public void eliminaContenuto() {
    }

    public void aggiungiAutore(String nome) {
    }

    public void aggiungiCollaboratore(String nome) {
    }

    public void rimuoviAllegato(Allegato a) {
    }

    public void aggiornaVisibilita(String policy) {
    }

    public byte[] getAnteprima() {
        return null;
    }

    public boolean checkFormato() {
        return false;
    }

    public List<Link> getLink() {
        return linkAssociati;
    }

    public void setLink(Link l) {
    }

    public java.util.UUID getId() {
        return id;
    }

    public void setId(java.util.UUID id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getPolicyVisibilita() {
        return policyVisibilita;
    }

    public void setPolicyVisibilita(String policyVisibilita) {
        this.policyVisibilita = policyVisibilita;
    }

    public Integer getNumeroVisualizzazioni() {
        return numeroVisualizzazioni;
    }

    public void setNumeroVisualizzazioni(Integer numeroVisualizzazioni) {
        this.numeroVisualizzazioni = numeroVisualizzazioni;
    }

    public Profilo getProfilo() {
        return profilo;
    }

    public void setProfilo(Profilo profilo) {
        this.profilo = profilo;
    }

    public List<Allegato> getAllegati() {
        return allegati;
    }

    public void setAllegati(List<Allegato> allegati) {
        this.allegati = allegati;
    }

    public List<String> getAutori() {
        return autori;
    }

    public void setAutori(List<String> autori) {
        this.autori = autori;
    }

    public List<String> getCollaboratori() {
        return collaboratori;
    }

    public void setCollaboratori(List<String> collaboratori) {
        this.collaboratori = collaboratori;
    }

    public List<Link> getLinkAssociati() {
        return linkAssociati;
    }

    public void setLinkAssociati(List<Link> linkAssociati) {
        this.linkAssociati = linkAssociati;
    }

    public List<Gruppo> getGruppi() {
        return gruppi;
    }

    public void setGruppi(List<Gruppo> gruppi) {
        this.gruppi = gruppi;
    }

    public java.time.LocalDateTime getDataCaricamento() {
        return dataCaricamento;
    }

    public void setDataCaricamento(java.time.LocalDateTime dataCaricamento) {
        this.dataCaricamento = dataCaricamento;
    }
}
