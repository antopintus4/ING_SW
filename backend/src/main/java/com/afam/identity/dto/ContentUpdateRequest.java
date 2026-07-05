package com.afam.identity.dto;

public class ContentUpdateRequest {
    private String titolo;
    private String policyVisibilita;
    private String autori;
    private String collaboratori;
    private String descrizione;

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getPolicyVisibilita() {
        return policyVisibilita;
    }

    public void setPolicyVisibilita(String policyVisibilita) {
        this.policyVisibilita = policyVisibilita;
    }

    public String getAutori() {
        return autori;
    }

    public void setAutori(String autori) {
        this.autori = autori;
    }

    public String getCollaboratori() {
        return collaboratori;
    }

    public void setCollaboratori(String collaboratori) {
        this.collaboratori = collaboratori;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
}
