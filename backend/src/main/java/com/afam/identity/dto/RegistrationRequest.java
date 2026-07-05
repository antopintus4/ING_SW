package com.afam.identity.dto;

public class RegistrationRequest {
    private String username;
    private String email;
    private String password;
    private String nome;
    private String cognome;
    private String sesso;
    private String dataNascita;
    private String cittaNascita;
    private String codiceFiscale;
    private String istituzione;
    private String dominioIstituzionale;
    private String matricola;
    private String corsoDiStudi;
    private String annoAccademico;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getSesso() { return sesso; }
    public void setSesso(String sesso) { this.sesso = sesso; }

    public String getDataNascita() { return dataNascita; }
    public void setDataNascita(String dataNascita) { this.dataNascita = dataNascita; }

    public String getCittaNascita() { return cittaNascita; }
    public void setCittaNascita(String cittaNascita) { this.cittaNascita = cittaNascita; }

    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }

    public String getIstituzione() { return istituzione; }
    public void setIstituzione(String istituzione) { this.istituzione = istituzione; }

    public String getDominioIstituzionale() { return dominioIstituzionale; }
    public void setDominioIstituzionale(String dominioIstituzionale) { this.dominioIstituzionale = dominioIstituzionale; }

    public String getMatricola() { return matricola; }
    public void setMatricola(String matricola) { this.matricola = matricola; }

    public String getCorsoDiStudi() { return corsoDiStudi; }
    public void setCorsoDiStudi(String corsoDiStudi) { this.corsoDiStudi = corsoDiStudi; }

    public String getAnnoAccademico() { return annoAccademico; }
    public void setAnnoAccademico(String annoAccademico) { this.annoAccademico = annoAccademico; }
}
