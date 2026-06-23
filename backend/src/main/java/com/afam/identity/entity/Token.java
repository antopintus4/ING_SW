package com.afam.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(nullable = false, unique = true)
    private String valore;

    @Column(nullable = false)
    private LocalDateTime scadenza;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "access_time")
    private LocalDateTime accessTime;

    @ManyToOne
    @JoinColumn(name = "utente_uuid", referencedColumnName = "uuid", nullable = false)
    private UtenteAfam utenteAfam;

    // Metodi UML (segnaposto per aderenza 100% al RAD)
    public void generaToken() {}
    public void checkToken() {}
    public void isScaduto() {}
    public void inviaViaMail() {}

    public java.util.UUID getId() { return id; }
    public void setId(java.util.UUID id) { this.id = id; }
    public String getValore() { return valore; }
    public void setValore(String valore) { this.valore = valore; }
    public LocalDateTime getScadenza() { return scadenza; }
    public void setScadenza(LocalDateTime scadenza) { this.scadenza = scadenza; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDateTime getAccessTime() { return accessTime; }
    public void setAccessTime(LocalDateTime accessTime) { this.accessTime = accessTime; }
    public UtenteAfam getUtenteAfam() { return utenteAfam; }
    public void setUtenteAfam(UtenteAfam utenteAfam) { this.utenteAfam = utenteAfam; }
}
