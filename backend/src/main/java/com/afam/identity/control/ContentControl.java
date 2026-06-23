package com.afam.identity.control;

import com.afam.identity.boundary.AllegatoDBMSBoundary;
import com.afam.identity.boundary.ContenutoDBMSBoundary;
import com.afam.identity.boundary.GruppoDBMSBoundary;
import com.afam.identity.boundary.ProfiloDBMSBoundary;
import com.afam.identity.boundary.UtenteAfamDBMSBoundary;
import com.afam.identity.entity.Allegato;
import com.afam.identity.entity.Contenuto;
import com.afam.identity.entity.Gruppo;
import com.afam.identity.entity.Profilo;
import com.afam.identity.entity.UtenteAfam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*") // In environment dev
public class ContentControl {

    @Autowired
    private ContenutoDBMSBoundary contenutoDBMSBoundary;

    @Autowired
    private GruppoDBMSBoundary gruppoDBMSBoundary;

    @Autowired
    private ProfiloDBMSBoundary profiloDBMSBoundary;

    @Autowired
    private UtenteAfamDBMSBoundary utenteAfamDBMSBoundary;

    private final String UPLOAD_DIR = "uploads";

    private Optional<Profilo> getProfiloAttuale() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return Optional.empty();
        String username = authentication.getName();
        Optional<UtenteAfam> utenteOpt = utenteAfamDBMSBoundary.findByUsername(username);
        if (utenteOpt.isPresent()) {
            return profiloDBMSBoundary.findByUtenteAfamUuid(utenteOpt.get().getUuid());
        }
        return Optional.empty();
    }

    // ==========================================
    // LOGICA CONTENUTI
    // ==========================================

    @GetMapping("/api/contenuti")
    public ResponseEntity<?> getMieiContenuti() {
        Optional<Profilo> profiloOpt = getProfiloAttuale();
        if (profiloOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Profilo non trovato");
        }
        List<Contenuto> contenuti = contenutoDBMSBoundary.findByProfilo(profiloOpt.get());
        return ResponseEntity.ok(contenuti);
    }

    @DeleteMapping("/api/contenuti/{id}")
    public ResponseEntity<?> eliminaContenuto(@PathVariable Long id) {
        Optional<Contenuto> contenutoOpt = contenutoDBMSBoundary.findById(id);
        if (contenutoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenuto non trovato");
        }

        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty() || !contenutoOpt.get().getProfilo().getId().equals(pOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato: non sei il proprietario");
        }

        // Cancellazione fisica file
        String uuid = pOpt.get().getUtenteAfam().getUuid();
        if (contenutoOpt.get().getAllegati() != null) {
            for(Allegato a : contenutoOpt.get().getAllegati()) {
                 try {
                     Files.deleteIfExists(Paths.get(UPLOAD_DIR, uuid, a.getUrlFile()));
                 } catch(IOException e) {
                     e.printStackTrace();
                 }
            }
        }

        contenutoDBMSBoundary.delete(contenutoOpt.get());
        return ResponseEntity.ok("Contenuto eliminato con successo");
    }

    // ==========================================
    // LOGICA GRUPPI
    // ==========================================

    @PostMapping("/api/gruppi")
    public ResponseEntity<?> creaGruppo(@RequestBody Gruppo gruppo) {
        Optional<Profilo> profiloOpt = getProfiloAttuale();
        if (profiloOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        gruppo.setProfilo(profiloOpt.get());
        Gruppo saved = gruppoDBMSBoundary.save(gruppo);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/api/gruppi")
    public ResponseEntity<List<Gruppo>> getMieiGruppi() {
        Optional<Profilo> profiloOpt = getProfiloAttuale();
        if (profiloOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Gruppo> gruppi = gruppoDBMSBoundary.findByProfiloId(profiloOpt.get().getId());
        return ResponseEntity.ok(gruppi);
    }

    @GetMapping("/api/gruppi/{id}")
    public ResponseEntity<Gruppo> getDettaglioGruppo(@PathVariable Long id) {
        Optional<Profilo> profiloOpt = getProfiloAttuale();
        if (profiloOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Gruppo> opt = gruppoDBMSBoundary.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        
        Gruppo g = opt.get();
        // Solo il proprietario può vedere il dettaglio per ora
        if (!g.getProfilo().getId().equals(profiloOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(g);
    }

    @PostMapping("/api/gruppi/{gruppoId}/contenuti/{contenutoId}")
    public ResponseEntity<?> aggregaContenuto(@PathVariable Long gruppoId, 
                                              @PathVariable Long contenutoId) {
        Optional<Profilo> profiloOpt = getProfiloAttuale();
        if (profiloOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Gruppo> optG = gruppoDBMSBoundary.findById(gruppoId);
        Optional<Contenuto> optC = contenutoDBMSBoundary.findById(contenutoId);

        if (optG.isEmpty() || optC.isEmpty()) return ResponseEntity.notFound().build();

        Gruppo g = optG.get();
        Contenuto c = optC.get();

        if (!g.getProfilo().getId().equals(profiloOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Verifica se il contenuto non sia già presente
        if (g.getListaContenuti() != null && g.getListaContenuti().stream().anyMatch(cont -> cont.getId().equals(c.getId()))) {
            return ResponseEntity.badRequest().body("Contenuto già aggregato al gruppo.");
        }

        if (g.getListaContenuti() == null) {
            g.setListaContenuti(new java.util.ArrayList<>());
        }

        g.getListaContenuti().add(c);
        gruppoDBMSBoundary.save(g);

        return ResponseEntity.ok(g);
    }

    @DeleteMapping("/api/gruppi/{id}")
    public ResponseEntity<?> eliminaGruppo(@PathVariable Long id) {
        Optional<Profilo> profiloOpt = getProfiloAttuale();
        if (profiloOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Gruppo> optG = gruppoDBMSBoundary.findById(id);
        if (optG.isEmpty()) return ResponseEntity.notFound().build();

        Gruppo g = optG.get();
        if (!g.getProfilo().getId().equals(profiloOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        gruppoDBMSBoundary.delete(g);
        return ResponseEntity.ok().build();
    }
}
