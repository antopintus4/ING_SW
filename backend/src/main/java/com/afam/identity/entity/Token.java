package com.afam.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String valore;

    @Column(nullable = false)
    private LocalDateTime scadenza;

    @Column(nullable = false, length = 50)
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "utente_uuid", referencedColumnName = "uuid", nullable = false)
    private UtenteAfam utenteAfam;

    // Metodi UML (segnaposto per aderenza 100% al RAD)
    public void generaToken() {}
    public void checkToken() {}
    public void isScaduto() {}
    public void inviaViaMail() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getValore() { return valore; }
    public void setValore(String valore) { this.valore = valore; }
    public LocalDateTime getScadenza() { return scadenza; }
    public void setScadenza(LocalDateTime scadenza) { this.scadenza = scadenza; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public UtenteAfam getUtenteAfam() { return utenteAfam; }
    public void setUtenteAfam(UtenteAfam utenteAfam) { this.utenteAfam = utenteAfam; }
}
