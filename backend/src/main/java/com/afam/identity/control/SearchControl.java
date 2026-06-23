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

        // 1. Ricerca Profili
        List<Profilo> profili = profiloDBMSBoundary.findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(q, q);

        // 2. Ricerca Contenuti (SOLO PUBBLICI)
        List<Contenuto> contenuti = contenutoDBMSBoundary.findByTitoloContainingIgnoreCaseAndPolicyVisibilita(q, "Pubblico");

        // 3. Ricerca Gruppi
        List<Gruppo> gruppi = gruppoDBMSBoundary.findByNomeContainingIgnoreCase(q);

        Map<String, Object> results = new HashMap<>();
        results.put("utenti", profili);
        results.put("contenuti", contenuti);
        results.put("gruppi", gruppi);

        return ResponseEntity.ok(results);
    }
}
