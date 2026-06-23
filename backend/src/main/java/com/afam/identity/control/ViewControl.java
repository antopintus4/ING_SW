package com.afam.identity.control;

import com.afam.identity.boundary.AllegatoDBMSBoundary;
import com.afam.identity.boundary.ContenutoDBMSBoundary;
import com.afam.identity.boundary.ProfiloDBMSBoundary;
import com.afam.identity.boundary.UtenteAfamDBMSBoundary;
import com.afam.identity.entity.Allegato;
import com.afam.identity.entity.Contenuto;
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
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*") // In environment dev
public class ViewControl {

    @Autowired
    private ContenutoDBMSBoundary contenutoDBMSBoundary;

    @Autowired
    private AllegatoDBMSBoundary allegatoDBMSBoundary;

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

    @GetMapping("/api/contenuti/{id}")
    public ResponseEntity<?> getDettaglioContenuto(@PathVariable java.util.UUID id) {
        Optional<Contenuto> contenutoOpt = contenutoDBMSBoundary.findById(id);
        if (contenutoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenuto non trovato");
        }
        
        // Verifica proprietario (opzionale se ci sono policy di condivisione)
        Contenuto c = contenutoOpt.get();
        Optional<Profilo> pOpt = getProfiloAttuale();
        if (pOpt.isEmpty() || !c.getProfilo().getId().equals(pOpt.get().getId())) {
            // Se non è il proprietario e policy = privato
            if ("Privato".equalsIgnoreCase(c.getPolicyVisibilita())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato");
            }
        }
        
        return ResponseEntity.ok(c);
    }

    @GetMapping("/api/contenuti/public/{id}")
    public ResponseEntity<?> getPublicContenuto(@PathVariable java.util.UUID id) {
        Optional<Contenuto> contenutoOpt = contenutoDBMSBoundary.findById(id);
        if (contenutoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenuto non trovato");
        }
        
        Contenuto c = contenutoOpt.get();
        if (!"Pubblico".equalsIgnoreCase(c.getPolicyVisibilita())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non si dispone dell'autorizzazione necessaria per visualizzare questo contenuto.");
        }
        
        return ResponseEntity.ok(c);
    }

    @GetMapping("/api/contenuti/download/{allegatoId}/file")
    public ResponseEntity<?> downloadFile(@PathVariable java.util.UUID allegatoId) {
        Optional<Allegato> allegatoOpt = allegatoDBMSBoundary.findById(allegatoId);
        if (allegatoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Allegato non trovato");
        }

        Allegato allegato = allegatoOpt.get();
        Contenuto c = allegato.getContenuto();
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
