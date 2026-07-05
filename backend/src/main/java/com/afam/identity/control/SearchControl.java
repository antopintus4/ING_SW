package com.afam.identity.control;

import com.afam.identity.boundary.ContenutoDBMSBoundary;
import com.afam.identity.boundary.GruppoDBMSBoundary;
import com.afam.identity.boundary.ProfiloDBMSBoundary;
import com.afam.identity.entity.Contenuto;
import com.afam.identity.entity.Gruppo;
import com.afam.identity.entity.Profilo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*") // In environment dev
public class SearchControl {

    @Autowired
    private ProfiloDBMSBoundary profiloDBMSBoundary;

    @Autowired
    private ContenutoDBMSBoundary contenutoDBMSBoundary;

    @Autowired
    private GruppoDBMSBoundary gruppoDBMSBoundary;

    @GetMapping
    public ResponseEntity<?> ricercaGlobale(@RequestParam String q) {
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.badRequest().body("La query deve contenere almeno 2 caratteri.");
        }

        // Normalizza l'eventuale codifica URL del due punti (%3a o %3A)
        String queryTrimmed = q.trim().replace("%3a", ":").replace("%3A", ":");
        String queryLower = queryTrimmed.toLowerCase();
        
        List<Profilo> profili = new java.util.ArrayList<>();
        List<Contenuto> contenuti = new java.util.ArrayList<>();
        List<Gruppo> gruppi = new java.util.ArrayList<>();

        // Caso 1: Ricerca specifica utente "user:"
        if (queryLower.startsWith("user:")) {
            String val = queryTrimmed.substring(5).trim();
            if (val.length() >= 2) {
                final String searchVal = val.toLowerCase();
                profili = profiloDBMSBoundary.findAll().stream()
                        .filter(p -> (p.getNome() != null && p.getNome().toLowerCase().contains(searchVal))
                                || (p.getCognome() != null && p.getCognome().toLowerCase().contains(searchVal))
                                || (p.getUtenteAfam() != null && p.getUtenteAfam().getUsername().toLowerCase().contains(searchVal)))
                        .collect(java.util.stream.Collectors.toList());
            }
        } 
        // Caso 2: Ricerca specifica gruppo "group:"
        else if (queryLower.startsWith("group:")) {
            String val = queryTrimmed.substring(6).trim();
            if (val.length() >= 2) {
                gruppi = gruppoDBMSBoundary.findByNomeContainingIgnoreCase(val);
            }
        } 
        // Caso 3: Ricerca specifica autore "author:"
        else if (queryLower.startsWith("author:")) {
            String val = queryTrimmed.substring(7).trim();
            if (val.length() >= 2) {
                final String authorVal = val.toLowerCase();
                // Profili autore
                profili = profiloDBMSBoundary.findAll().stream()
                        .filter(p -> (p.getNome() != null && p.getNome().toLowerCase().contains(authorVal))
                                || (p.getCognome() != null && p.getCognome().toLowerCase().contains(authorVal)))
                        .collect(java.util.stream.Collectors.toList());
                // Contenuti associati (solo pubblici)
                contenuti = contenutoDBMSBoundary.findAll().stream()
                        .filter(c -> "Pubblico".equalsIgnoreCase(c.getPolicyVisibilita()))
                        .filter(c -> (c.getAutori() != null && c.getAutori().stream().anyMatch(a -> a.toLowerCase().contains(authorVal)))
                                || (c.getProfilo() != null && (c.getProfilo().getNome().toLowerCase().contains(authorVal) 
                                                               || c.getProfilo().getCognome().toLowerCase().contains(authorVal))))
                        .collect(java.util.stream.Collectors.toList());
            }
        } 
        // Caso 4: Ricerca specifica collaboratore "collab:" o "collaborator:"
        else if (queryLower.startsWith("collab:") || queryLower.startsWith("collaborator:")) {
            int colonIndex = queryTrimmed.indexOf(":");
            String val = queryTrimmed.substring(colonIndex + 1).trim();
            if (val.length() >= 2) {
                final String collabVal = val.toLowerCase();
                contenuti = contenutoDBMSBoundary.findAll().stream()
                        .filter(c -> "Pubblico".equalsIgnoreCase(c.getPolicyVisibilita()))
                        .filter(c -> c.getCollaboratori() != null && c.getCollaboratori().stream().anyMatch(col -> col.toLowerCase().contains(collabVal)))
                        .collect(java.util.stream.Collectors.toList());
            }
        } 
        // Caso di default: Ricerca globale senza prefisso
        else {
            profili = profiloDBMSBoundary.findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(queryTrimmed, queryTrimmed);
            contenuti = contenutoDBMSBoundary.findByTitoloContainingIgnoreCaseAndPolicyVisibilita(queryTrimmed, "Pubblico");
            gruppi = gruppoDBMSBoundary.findByNomeContainingIgnoreCase(queryTrimmed);
        }

        Map<String, Object> results = new HashMap<>();
        results.put("utenti", profili);
        results.put("contenuti", contenuti);
        results.put("gruppi", gruppi);

        return ResponseEntity.ok(results);
    }
}
