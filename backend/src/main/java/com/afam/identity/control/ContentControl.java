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
import com.afam.identity.middleware.Validator;
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
    private AllegatoDBMSBoundary allegatoDBMSBoundary;

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

    @PutMapping("/api/contenuti/{id}")
    public ResponseEntity<?> aggiornaContenuto(@PathVariable java.util.UUID id, @RequestBody com.afam.identity.dto.ContentUpdateRequest request) {
        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Contenuto> contenutoOpt = contenutoDBMSBoundary.findById(id);
        if (contenutoOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenuto non trovato");

        Contenuto c = contenutoOpt.get();
        if (!c.getProfilo().getId().equals(pOpt.get().getId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato");

        c.setTitolo(Validator.sanitize(request.getTitolo(), false));
        c.setPolicyVisibilita(Validator.sanitize(request.getPolicyVisibilita(), false));
        
        if (request.getAutori() != null && !request.getAutori().trim().isEmpty()) {
            c.setAutori(new java.util.ArrayList<>(java.util.List.of(Validator.sanitize(request.getAutori(), false).split(","))));
        } else {
            c.setAutori(new java.util.ArrayList<>(java.util.List.of(pOpt.get().getNome() + " " + pOpt.get().getCognome())));
        }

        if (request.getCollaboratori() != null && !request.getCollaboratori().trim().isEmpty()) {
            c.setCollaboratori(new java.util.ArrayList<>(java.util.List.of(Validator.sanitize(request.getCollaboratori(), false).split(","))));
        } else {
            c.setCollaboratori(new java.util.ArrayList<>());
        }

        String reqDesc = Validator.sanitize(request.getDescrizione(), false);
        if (reqDesc != null && !reqDesc.trim().isEmpty()) {
            if (reqDesc.startsWith("/delete")) {
                String remainder = reqDesc.substring(7).trim();
                c.setDescrizione(remainder);
            } else {
                String oldDesc = c.getDescrizione() != null ? c.getDescrizione() : "";
                c.setDescrizione(oldDesc + (oldDesc.isEmpty() ? "" : "\n") + reqDesc);
            }
        }

        contenutoDBMSBoundary.save(c);
        return ResponseEntity.ok(c);
    }

    @PostMapping("/api/contenuti/{id}/allegati")
    public ResponseEntity<?> aggiungiAllegato(@PathVariable java.util.UUID id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Contenuto> contenutoOpt = contenutoDBMSBoundary.findById(id);
        if (contenutoOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenuto non trovato");

        Contenuto c = contenutoOpt.get();
        if (!c.getProfilo().getId().equals(pOpt.get().getId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato");

        try {
            java.nio.file.Path userUploadPath = Paths.get(UPLOAD_DIR, pOpt.get().getUtenteAfam().getUuid().toString());
            if (!Files.exists(userUploadPath)) {
                Files.createDirectories(userUploadPath);
            }
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            java.nio.file.Path filePath = userUploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            Allegato allegato = new Allegato();
            allegato.setUrlFile(filename);
            allegato.setContenuto(c);
            allegatoDBMSBoundary.save(allegato);

            if (c.getAllegati() == null) {
                c.setAllegati(new java.util.ArrayList<>());
            }
            c.getAllegati().add(allegato);
            contenutoDBMSBoundary.save(c);

            return ResponseEntity.ok(allegato);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore salvataggio file");
        }
    }

    @DeleteMapping("/api/contenuti/{id}/allegati/{allegatoId}")
    public ResponseEntity<?> eliminaAllegato(@PathVariable java.util.UUID id, @PathVariable java.util.UUID allegatoId) {
        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Contenuto> contenutoOpt = contenutoDBMSBoundary.findById(id);
        if (contenutoOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenuto non trovato");

        Contenuto c = contenutoOpt.get();
        if (!c.getProfilo().getId().equals(pOpt.get().getId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato");

        Optional<Allegato> allegatoOpt = allegatoDBMSBoundary.findById(allegatoId);
        if (allegatoOpt.isEmpty() || !allegatoOpt.get().getContenuto().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Allegato non trovato in questo contenuto");
        }

        String uuid = pOpt.get().getUtenteAfam().getUuid().toString();
        try {
            Files.deleteIfExists(Paths.get(UPLOAD_DIR, uuid, allegatoOpt.get().getUrlFile()));
        } catch(IOException e) {
            e.printStackTrace();
        }

        c.getAllegati().removeIf(a -> a.getId().equals(allegatoId));
        allegatoDBMSBoundary.delete(allegatoOpt.get());
        contenutoDBMSBoundary.save(c);

        return ResponseEntity.ok("Allegato eliminato");
    }

    @DeleteMapping("/api/contenuti/{id}")
    public ResponseEntity<?> eliminaContenuto(@PathVariable java.util.UUID id) {
        Optional<Contenuto> contenutoOpt = contenutoDBMSBoundary.findById(id);
        if (contenutoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenuto non trovato");
        }

        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty() || !contenutoOpt.get().getProfilo().getId().equals(pOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato: non sei il proprietario");
        }

        // Cancellazione fisica file
        String uuid = pOpt.get().getUtenteAfam().getUuid().toString();
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
    public ResponseEntity<Gruppo> getDettaglioGruppo(@PathVariable java.util.UUID id) {
        Optional<Profilo> profiloOpt = getProfiloAttuale();
        if (profiloOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Gruppo> opt = gruppoDBMSBoundary.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        
        Gruppo g = opt.get();
        if (!g.getProfilo().getId().equals(profiloOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(g);
    }

    @PutMapping("/api/gruppi/{id}")
    public ResponseEntity<?> aggiornaGruppo(@PathVariable java.util.UUID id, @RequestBody Gruppo gruppoModificato) {
        Optional<Profilo> profiloOpt = getProfiloAttuale();
        if (profiloOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Gruppo> opt = gruppoDBMSBoundary.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Gruppo g = opt.get();
        if (!g.getProfilo().getId().equals(profiloOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        g.setNome(gruppoModificato.getNome());
        Gruppo saved = gruppoDBMSBoundary.save(g);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/api/gruppi/{gruppoId}/contenuti/{contenutoId}")
    public ResponseEntity<?> aggregaContenuto(@PathVariable java.util.UUID gruppoId, 
                                              @PathVariable java.util.UUID contenutoId) {
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

    @DeleteMapping("/api/gruppi/{gruppoId}/contenuti/{contenutoId}")
    public ResponseEntity<?> rimuoviContenutoDaGruppo(@PathVariable java.util.UUID gruppoId, 
                                                      @PathVariable java.util.UUID contenutoId) {
        Optional<Profilo> profiloOpt = getProfiloAttuale();
        if (profiloOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Gruppo> optG = gruppoDBMSBoundary.findById(gruppoId);
        if (optG.isEmpty()) return ResponseEntity.notFound().build();

        Gruppo g = optG.get();
        if (!g.getProfilo().getId().equals(profiloOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (g.getListaContenuti() != null) {
            boolean removed = g.getListaContenuti().removeIf(c -> c.getId().equals(contenutoId));
            if (removed) {
                gruppoDBMSBoundary.save(g);
            }
        }
        return ResponseEntity.ok(g);
    }

    @DeleteMapping("/api/gruppi/{id}")
    public ResponseEntity<?> eliminaGruppo(@PathVariable java.util.UUID id) {
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
