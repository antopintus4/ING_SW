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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*") // In environment dev
public class UploadControl {

    @Autowired
    private ContenutoDBMSBoundary contenutoDBMSBoundary;

    @Autowired
    private AllegatoDBMSBoundary allegatoDBMSBoundary;

    @Autowired
    private ProfiloDBMSBoundary profiloDBMSBoundary;

    @Autowired
    private UtenteAfamDBMSBoundary utenteAfamDBMSBoundary;

    private final String UPLOAD_DIR = "uploads";

    @PostMapping("/api/contenuti/upload")
    public ResponseEntity<?> uploadContenuto(
            @RequestParam("file") MultipartFile[] files,
            @RequestParam("titolo") String titolo,
            @RequestParam(value = "descrizioneFile", required = false) MultipartFile descrizioneFile,
            @RequestParam("policyVisibilita") String policyVisibilita,
            @RequestParam(value = "autori", required = false) String autori,
            @RequestParam(value = "collaboratori", required = false) String collaboratori) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<UtenteAfam> utenteOpt = utenteAfamDBMSBoundary.findByUsername(username);
            if (utenteOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non trovato");
            }

            UtenteAfam utente = utenteOpt.get();
            Optional<Profilo> profiloOpt = profiloDBMSBoundary.findByUtenteAfamUuid(utente.getUuid());

            if (profiloOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Profilo non trovato per l'utente");
            }

            Profilo profilo = profiloOpt.get();

            // Leggi il file di descrizione
            String descrizione = "";
            if (descrizioneFile != null && !descrizioneFile.isEmpty()) {
                if (!"text/plain".equals(descrizioneFile.getContentType())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Il file di descrizione deve essere di tipo testo.");
                }
                descrizione = new String(descrizioneFile.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }

            // 1. Crea la cartella utente se non esiste
            Path userUploadPath = Paths.get(UPLOAD_DIR, utente.getUuid().toString());
            if (!Files.exists(userUploadPath)) {
                Files.createDirectories(userUploadPath);
            }

            if (files == null || files.length == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nessun file selezionato");
            }

            // 3. Salva nel DB (Contenuto e Allegati)
            Contenuto contenuto = new Contenuto();
            contenuto.setTitolo(com.afam.identity.middleware.Validator.sanitize(titolo, false));
            contenuto.setDescrizione(com.afam.identity.middleware.Validator.sanitize(descrizione, false));
            contenuto.setPolicyVisibilita(com.afam.identity.middleware.Validator.sanitize(policyVisibilita, false));
            contenuto.setTipo(files[0].getContentType());
            contenuto.setProfilo(profilo);

            if (autori != null && !autori.trim().isEmpty()) {
                contenuto.setAutori(new ArrayList<>(List.of(com.afam.identity.middleware.Validator.sanitize(autori, false).split(","))));
            } else {
                contenuto.setAutori(new ArrayList<>(List.of(profilo.getNome() + " " + profilo.getCognome())));
            }
            if (collaboratori != null && !collaboratori.trim().isEmpty()) {
                contenuto.setCollaboratori(new ArrayList<>(List.of(com.afam.identity.middleware.Validator.sanitize(collaboratori, false).split(","))));
            }

            Contenuto savedContenuto = contenutoDBMSBoundary.save(contenuto);
            savedContenuto.setAllegati(new ArrayList<>());

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                // Salva il file
                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = userUploadPath.resolve(filename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                Allegato allegato = new Allegato();
                allegato.setUrlFile(filename); 
                allegato.setContenuto(savedContenuto);
                allegatoDBMSBoundary.save(allegato);

                savedContenuto.getAllegati().add(allegato);
            }

            return ResponseEntity.ok("Contenuto caricato con successo con ID: " + savedContenuto.getId());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore nel salvataggio del file: " + e.getMessage());
        }
    }
}
