package com.afam.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "profilo")
public class Profilo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @OneToOne
    @JoinColumn(name = "utente_uuid", referencedColumnName = "uuid", unique = true, nullable = false)
    private UtenteAfam utenteAfam;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @ElementCollection
    @CollectionTable(name = "profilo_interessi", joinColumns = @JoinColumn(name = "profilo_id"))
    @Column(name = "interesse")
    private List<String> interessi;

    @ElementCollection
    @CollectionTable(name = "profilo_competenze", joinColumns = @JoinColumn(name = "profilo_id"))
    @Column(name = "competenza")
    private List<String> competenze;

    @Column(name = "policy_visibilita", length = 50)
    private String policyVisibilita;

    @Column(name = "foto_profilo")
    private String fotoProfilo;

    // Campi flat per Anagrafica
    @Column(length = 50)
    private String nome;

    @Column(length = 50)
    private String cognome;

    @Column(name = "data_nascita")
    private java.time.LocalDate dataNascita;

    @Column(name = "codice_fiscale", unique = true, length = 16)
    private String codiceFiscale;

    @Column(length = 100)
    private String citta;

    @Column(length = 255)
    private String indirizzo;

    @Column(length = 20)
    private String telefono;

    // Campi flat per DatiAccademici
    @Column(length = 100)
    private String istituzione;

    @Column(name = "dominio_istituzionale", length = 100)
    private String dominioIstituzionale;

    @Column(length = 50)
    private String matricola;

    @Column(name = "corso_di_studi", length = 100)
    private String corsoDiStudi;

    @Column(name = "anno_accademico", length = 20)
    private String annoAccademico;

    // Metodi UML (segnaposto per aderenza 100% al RAD)
    public void getProfilo() {}
    public void modifyAnagrafica() {}
    public void modifyDatiAccademici() {}
    public void modifyAltreInformazioni() {}
    public void aggiornaVisibilitaProfilo() {}
    public void calculateCF() {}
    public void validateBirthday() {}

    public java.util.UUID getId() { return id; }
    public void setId(java.util.UUID id) { this.id = id; }
    public UtenteAfam getUtenteAfam() { return utenteAfam; }
    public void setUtenteAfam(UtenteAfam utenteAfam) { this.utenteAfam = utenteAfam; }
    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public List<String> getInteressi() { return interessi; }
    public void setInteressi(List<String> interessi) { this.interessi = interessi; }
    public List<String> getCompetenze() { return competenze; }
    public void setCompetenze(List<String> competenze) { this.competenze = competenze; }
    public String getPolicyVisibilita() { return policyVisibilita; }
    public void setPolicyVisibilita(String policyVisibilita) { this.policyVisibilita = policyVisibilita; }
    public String getFotoProfilo() { return fotoProfilo; }
    public void setFotoProfilo(String fotoProfilo) { this.fotoProfilo = fotoProfilo; }

    // Getters and Setters per Anagrafica
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public java.time.LocalDate getDataNascita() { return dataNascita; }
    public void setDataNascita(java.time.LocalDate dataNascita) { this.dataNascita = dataNascita; }
    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }
    public String getCitta() { return citta; }
    public void setCitta(String citta) { this.citta = citta; }
    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    // Getters and Setters per DatiAccademici
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
