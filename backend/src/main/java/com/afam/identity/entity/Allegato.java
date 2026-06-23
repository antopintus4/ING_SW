package com.afam.identity.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "allegato")
public class Allegato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_file", nullable = false)
    private String urlFile;

    @ManyToOne
    @JoinColumn(name = "contenuto_id", nullable = false)
    private Contenuto contenuto;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUrlFile() { return urlFile; }
    public void setUrlFile(String urlFile) { this.urlFile = urlFile; }
    public Contenuto getContenuto() { return contenuto; }
    public void setContenuto(Contenuto contenuto) { this.contenuto = contenuto; }
}
