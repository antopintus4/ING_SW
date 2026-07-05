package com.afam.identity.dto;

import java.util.List;

public class OtherEditsDTO {
    private String policyVisibilita;
    private String descrizione;
    private List<String> interessi;
    private List<String> competenze;

    public String getPolicyVisibilita() {
        return policyVisibilita;
    }

    public void setPolicyVisibilita(String policyVisibilita) {
        this.policyVisibilita = policyVisibilita;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public List<String> getInteressi() {
        return interessi;
    }

    public void setInteressi(List<String> interessi) {
        this.interessi = interessi;
    }

    public List<String> getCompetenze() {
        return competenze;
    }

    public void setCompetenze(List<String> competenze) {
        this.competenze = competenze;
    }
}
