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
        if (pOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        java.util.UUID contenutoId = java.util.UUID.fromString(request.get("contenutoId").toString());
        int daysToExpire = Integer.parseInt(request.get("daysToExpire").toString());

        Optional<Contenuto> cOpt = contenutoDBMSBoundary.findById(contenutoId);
        if (cOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenuto non trovato");

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
        if (pOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Link> links = linkDBMSBoundary.findByProfilo(pOpt.get());
        return ResponseEntity.ok(links);
    }

    @GetMapping("/{identificatore}")
    public ResponseEntity<?> getLinkDetails(@PathVariable String identificatore) {
        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Link> linkOpt = linkDBMSBoundary.findById(identificatore);
        if (linkOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Link link = linkOpt.get();
        if (!link.getProfilo().getId().equals(pOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato");
        }

        // Return a map including the details of the associated content (and its
        // attachments)
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("identificatoreLink", link.getIdentificatoreLink());
        response.put("dataScadenza", link.getDataScadenza());
        response.put("risorsaAssociata", link.getRisorsaAssociata());
        response.put("stato", link.getStato());
        response.put("impostazioni", link.getImpostazioni());
        response.put("numeroVisualizzazioni", link.getNumeroVisualizzazioni());

        if (link.getContenuto() != null) {
            Map<String, Object> cMap = new java.util.HashMap<>();
            cMap.put("id", link.getContenuto().getId());
            cMap.put("titolo", link.getContenuto().getTitolo());
            cMap.put("allegati", link.getContenuto().getAllegati());
            response.put("contenuto", cMap);
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{identificatore}")
    public ResponseEntity<?> updateLinkSettings(@PathVariable String identificatore,
            @RequestBody Map<String, Object> request) {
        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Link> linkOpt = linkDBMSBoundary.findById(identificatore);
        if (linkOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Link link = linkOpt.get();
        if (!link.getProfilo().getId().equals(pOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato");
        }

        if (request.containsKey("dataScadenza")) {
            String dataStr = request.get("dataScadenza").toString();
            // DataStr could be ISO string like "2026-07-12T12:00:00" or simple date
            // representation
            if (dataStr.contains("Z")) {
                dataStr = dataStr.substring(0, dataStr.indexOf("Z"));
            }
            if (dataStr.contains(".")) {
                dataStr = dataStr.substring(0, dataStr.indexOf("."));
            }
            try {
                LocalDateTime newScadenza = LocalDateTime.parse(dataStr);
                link.setDataScadenza(newScadenza);
            } catch (Exception e) {
                // Fallback / log error
            }
        }

        if (request.containsKey("risorseIncluse")) {
            List<?> risorse = (List<?>) request.get("risorseIncluse");
            StringBuilder json = new StringBuilder("{\"risorseIncluse\":[");
            for (int i = 0; i < risorse.size(); i++) {
                json.append("\"").append(risorse.get(i).toString()).append("\"");
                if (i < risorse.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]}");
            link.setImpostazioni(json.toString());
        } else {
            link.setImpostazioni(null);
        }

        linkDBMSBoundary.save(link);
        return ResponseEntity.ok(link);
    }

    @DeleteMapping("/{identificatore}")
    public ResponseEntity<?> revocaLink(@PathVariable String identificatore) {
        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Link> linkOpt = linkDBMSBoundary.findById(identificatore);
        if (linkOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Link link = linkOpt.get();
        if (!link.getProfilo().getId().equals(pOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato");
        }

        link.setStato("REVOCATO");
        linkDBMSBoundary.save(link);
        return ResponseEntity.ok("Link revocato con successo");
    }

    @Autowired
    private com.afam.identity.service.EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<?> inviaLinkViaMail(@RequestBody Map<String, String> request) {
        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String identificatore = request.get("identificatore");
        String recipient = request.get("email");

        Optional<UtenteAfam> targetUserOpt = utenteAfamDBMSBoundary.findByEmail(recipient);
        if (targetUserOpt.isEmpty()) {
            targetUserOpt = utenteAfamDBMSBoundary.findByUsername(recipient);
        }

        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", "Utente non trovato"));
        }

        Optional<Link> linkOpt = linkDBMSBoundary.findById(identificatore);
        if (linkOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Link link = linkOpt.get();
        if (!link.getProfilo().getId().equals(pOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "Non autorizzato"));
        }

        String toEmail = targetUserOpt.get().getEmail();
        String url = "http://localhost:4200/share/" + link.getIdentificatoreLink();
        emailService.sendDirectShareEmail(toEmail, url,
                link.getContenuto() != null ? link.getContenuto().getTitolo() : "Risorsa Condivisa");

        return ResponseEntity.ok(java.util.Map.of("message", "Condivisione avvenuta con successo"));
    }

    // ==========================================
    // ENDPOINT PUBBLICI (No Auth Richiesta)
    // ==========================================

    @GetMapping("/shared/{identificatore}")
    public ResponseEntity<?> visualizzaCondivisione(@PathVariable String identificatore) {
        Optional<Link> linkOpt = linkDBMSBoundary.findById(identificatore);
        if (linkOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Link inesistente");

        Link link = linkOpt.get();
        if ("REVOCATO".equals(link.getStato()) || link.getDataScadenza().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Questo link è scaduto o è stato revocato");
        }

        // Incrementa visite
        link.setNumeroVisualizzazioni(link.getNumeroVisualizzazioni() + 1);
        linkDBMSBoundary.save(link);

        Contenuto c = link.getContenuto();
        if (c != null) {
            c.setNumeroVisualizzazioni(c.getNumeroVisualizzazioni() + 1);
            contenutoDBMSBoundary.save(c);

            String settings = link.getImpostazioni();
            if (settings != null && settings.contains("risorseIncluse")) {
                List<Allegato> filtered = new java.util.ArrayList<>();
                for (Allegato a : c.getAllegati()) {
                    if (settings.contains(a.getId().toString())) {
                        filtered.add(a);
                    }
                }
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("id", c.getId());
                response.put("titolo", c.getTitolo());
                response.put("tipo", c.getTipo());
                response.put("descrizione", c.getDescrizione());
                response.put("policyVisibilita", c.getPolicyVisibilita());
                response.put("numeroVisualizzazioni", c.getNumeroVisualizzazioni());
                response.put("allegati", filtered);
                response.put("autori", c.getAutori());
                response.put("collaboratori", c.getCollaboratori());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.ok(c);
    }

    @GetMapping("/shared/{identificatore}/download")
    public ResponseEntity<?> downloadCondivisione(@PathVariable String identificatore,
            @RequestParam(required = false) java.util.UUID allegatoId) {
        Optional<Link> linkOpt = linkDBMSBoundary.findById(identificatore);
        if (linkOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Link inesistente");

        Link link = linkOpt.get();
        if ("REVOCATO".equals(link.getStato()) || link.getDataScadenza().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Questo link è scaduto o è stato revocato");
        }

        Contenuto c = link.getContenuto();
        if (c.getAllegati() == null || c.getAllegati().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nessun allegato presente");
        }

        String settings = link.getImpostazioni();
        List<Allegato> allowedAllegati = new java.util.ArrayList<>();
        for (Allegato a : c.getAllegati()) {
            if (settings == null || !settings.contains("risorseIncluse") || settings.contains(a.getId().toString())) {
                allowedAllegati.add(a);
            }
        }

        if (allowedAllegati.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nessuna risorsa disponibile per questo link.");
        }

        Allegato allegato = allowedAllegati.get(0);
        if (allegatoId != null) {
            Optional<Allegato> chosen = allowedAllegati.stream().filter(a -> a.getId().equals(allegatoId)).findFirst();
            if (chosen.isPresent()) {
                allegato = chosen.get();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Risorsa non disponibile per questo link.");
            }
        }

        String uuidProprietario = c.getProfilo().getUtenteAfam().getUuid().toString();

        try {
            Path file = Paths.get(UPLOAD_DIR, uuidProprietario, allegato.getUrlFile());
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = java.nio.file.Files.probeContentType(file);
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File fisico non trovato sul server");
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore nella lettura del file");
        } catch (java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore nella determinazione del tipo di file");
        }
    }
}
