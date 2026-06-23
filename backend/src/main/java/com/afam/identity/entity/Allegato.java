package com.afam.identity.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "allegato")
public class Allegato {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(name = "url_file", nullable = false)
    private String urlFile;

    @ManyToOne
    @JoinColumn(name = "contenuto_id", nullable = false)
    private Contenuto contenuto;

    public java.util.UUID getId() { return id; }
    public void setId(java.util.UUID id) { this.id = id; }
    public String getUrlFile() { return urlFile; }
    public void setUrlFile(String urlFile) { this.urlFile = urlFile; }
    public Contenuto getContenuto() { return contenuto; }
    public void setContenuto(Contenuto contenuto) { this.contenuto = contenuto; }
}
