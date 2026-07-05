package com.afam.identity.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "gruppo")
public class Gruppo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "profilo_id", nullable = false)
    private Profilo profilo;

    @ManyToMany
    @JoinTable(
        name = "gruppo_contenuto",
        joinColumns = @JoinColumn(name = "gruppo_id"),
        inverseJoinColumns = @JoinColumn(name = "contenuto_id")
    )
    protected List<Contenuto> listaContenuti;

    @JsonIgnore
    @OneToMany(mappedBy = "gruppo", cascade = CascadeType.ALL, orphanRemoval = true)
    protected List<Link> linkAssociati;

    // Metodi UML (segnaposto per aderenza 100% al RAD)
    public void creaGruppo() {}
    public void aggiungiContenuto(Contenuto c) {}
    public void rimuoviContenuto(Contenuto c) {}
    public boolean groupExist() { return false; }
    public List<Contenuto> getContenuti() { return listaContenuti; }
    public void eliminaGruppo() {}
    public List<Link> getLink() { return linkAssociati; }
    public void setLink(Link l) {}

    public java.util.UUID getId() { return id; }
    public void setId(java.util.UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Profilo getProfilo() { return profilo; }
    public void setProfilo(Profilo profilo) { this.profilo = profilo; }
    public List<Contenuto> getListaContenuti() { return listaContenuti; }
    public void setListaContenuti(List<Contenuto> listaContenuti) { this.listaContenuti = listaContenuti; }
    public List<Link> getLinkAssociati() { return linkAssociati; }
    public void setLinkAssociati(List<Link> linkAssociati) { this.linkAssociati = linkAssociati; }
}
