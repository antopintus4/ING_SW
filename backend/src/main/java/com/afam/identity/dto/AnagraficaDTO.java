package com.afam.identity.dto;

import java.time.LocalDate;

public class AnagraficaDTO {
    private String nome;
    private String cognome;
    private LocalDate dataNascita;
    private String codiceFiscale;
    private String citta;
    private String indirizzo;
    private String telefono;
    private String sesso;

    public String getSesso() { return sesso; }
    public void setSesso(String sesso) { this.sesso = sesso; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public LocalDate getDataNascita() { return dataNascita; }
    public void setDataNascita(LocalDate dataNascita) { this.dataNascita = dataNascita; }
    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }
    public String getCitta() { return citta; }
    public void setCitta(String citta) { this.citta = citta; }
    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
