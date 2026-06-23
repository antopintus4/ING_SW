package com.afam.identity.control;

import com.afam.identity.boundary.ContenutoDBMSBoundary;
import com.afam.identity.boundary.LinkDBMSBoundary;
import com.afam.identity.boundary.ProfiloDBMSBoundary;
import com.afam.identity.boundary.UtenteAfamDBMSBoundary;
import com.afam.identity.entity.Allegato;
import com.afam.identity.entity.Contenuto;
import com.afam.identity.entity.Link;
import com.afam.identity.entity.Profilo;
import com.afam.identity.entity.UtenteAfam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/links")
@CrossOrigin(origins = "*") // In environment dev
public class ShareControl {

    @Autowired
    private LinkDBMSBoundary linkDBMSBoundary;

    @Autowired
    private ContenutoDBMSBoundary contenutoDBMSBoundary;

    @Autowired
    private ProfiloDBMSBoundary profiloDBMSBoundary;

    @Autowired
    private UtenteAfamDBMSBoundary utenteAfamDBMSBoundary;

    private final String UPLOAD_DIR = "uploads";

    private Optional<Profilo> getProfiloAttuale() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<UtenteAfam> utenteOpt = utenteAfamDBMSBoundary.findByUsername(username);
        if (utenteOpt.isPresent()) {
            return profiloDBMSBoundary.findByUtenteAfamUuid(utenteOpt.get().getUuid());
        }
        return Optional.empty();
    }

    @PostMapping
    public ResponseEntity<?> creaLinkCondivisione(@RequestBody Map<String, Object> request) {
        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        java.util.UUID contenutoId = java.util.UUID.fromString(request.get("contenutoId").toString());
        int daysToExpire = Integer.parseInt(request.get("daysToExpire").toString());

        Optional<Contenuto> cOpt = contenutoDBMSBoundary.findById(contenutoId);
        if (cOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenuto non trovato");

        Contenuto c = cOpt.get();
        if (!c.getProfilo().getId().equals(pOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non sei il proprietario di questo contenuto");
        }

        Link link = new Link();
        link.setIdentificatoreLink(UUID.randomUUID().toString());
        link.setDataScadenza(LocalDateTime.now().plusDays(daysToExpire));
        link.setRisorsaAssociata("CONTENUTO");
        link.setStato("ATTIVO");
        link.setNumeroVisualizzazioni(0);
        link.setProfilo(pOpt.get());
        link.setContenuto(c);

        Link savedLink = linkDBMSBoundary.save(link);
        return ResponseEntity.ok(savedLink);
    }

    @GetMapping
    public ResponseEntity<?> getMieiLink() {
        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Link> links = linkDBMSBoundary.findByProfilo(pOpt.get());
        return ResponseEntity.ok(links);
    }

    @DeleteMapping("/{identificatore}")
    public ResponseEntity<?> revocaLink(@PathVariable String identificatore) {
        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Link> linkOpt = linkDBMSBoundary.findById(identificatore);
        if (linkOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Link link = linkOpt.get();
        if (!link.getProfilo().getId().equals(pOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato");
        }

        link.setStato("REVOCATO");
        linkDBMSBoundary.save(link);
        return ResponseEntity.ok("Link revocato con successo");
    }

    // ==========================================
    // ENDPOINT PUBBLICI (No Auth Richiesta)
    // ==========================================

    @GetMapping("/shared/{identificatore}")
    public ResponseEntity<?> visualizzaCondivisione(@PathVariable String identificatore) {
        Optional<Link> linkOpt = linkDBMSBoundary.findById(identificatore);
        if (linkOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Link inesistente");

        Link link = linkOpt.get();
        if ("REVOCATO".equals(link.getStato()) || link.getDataScadenza().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Questo link è scaduto o è stato revocato");
        }

        // Incrementa visite
        link.setNumeroVisualizzazioni(link.getNumeroVisualizzazioni() + 1);
        linkDBMSBoundary.save(link);

        return ResponseEntity.ok(link.getContenuto());
    }

    @GetMapping("/shared/{identificatore}/download")
    public ResponseEntity<?> downloadCondivisione(@PathVariable String identificatore) {
        Optional<Link> linkOpt = linkDBMSBoundary.findById(identificatore);
        if (linkOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Link inesistente");

        Link link = linkOpt.get();
        if ("REVOCATO".equals(link.getStato()) || link.getDataScadenza().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Questo link è scaduto o è stato revocato");
        }

        Contenuto c = link.getContenuto();
        if (c.getAllegati() == null || c.getAllegati().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nessun allegato presente");
        }

        Allegato allegato = c.getAllegati().get(0);
        String uuidProprietario = c.getProfilo().getUtenteAfam().getUuid().toString();

        try {
            Path file = Paths.get(UPLOAD_DIR, uuidProprietario, allegato.getUrlFile());
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File fisico non trovato sul server");
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore nella lettura del file");
        }
    }
}
