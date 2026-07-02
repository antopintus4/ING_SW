package com.afam.identity.control;

import com.afam.identity.boundary.ContenutoDBMSBoundary;
import com.afam.identity.boundary.ProfiloDBMSBoundary;
import com.afam.identity.boundary.UtenteAfamDBMSBoundary;
import com.afam.identity.dto.AnagraficaDTO;
import com.afam.identity.dto.AuthResponse;
import com.afam.identity.dto.CredentialsDTO;
import com.afam.identity.dto.PasswordDTO;
import com.afam.identity.dto.OtherEditsDTO;
import com.afam.identity.entity.Contenuto;
import com.afam.identity.entity.Profilo;
import com.afam.identity.entity.UtenteAfam;
import com.afam.identity.middleware.Sauron;
import com.afam.identity.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileControl {

    @Autowired
    private ProfiloDBMSBoundary profiloDBMSBoundary;

    @Autowired
    private UtenteAfamDBMSBoundary utenteAfamDBMSBoundary;

    @Autowired
    private ContenutoDBMSBoundary contenutoDBMSBoundary;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    private UtenteAfam getUtenteCorrente() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        String username = authentication.getName();
        return utenteAfamDBMSBoundary.findByUsername(username).orElse(null);
    }

    private Profilo getProfiloCorrente() {
        UtenteAfam utente = getUtenteCorrente();
        if (utente != null) {
            return profiloDBMSBoundary.findByUtenteAfamUuid(utente.getUuid()).orElse(null);
        }
        return null;
    }

    @GetMapping("/me")
    public ResponseEntity<Profilo> getMe() {
        Profilo profilo = getProfiloCorrente();
        if (profilo == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(profilo);
    }

    @PutMapping("/anagrafica")
    public ResponseEntity<?> updateAnagrafica(@RequestBody AnagraficaDTO request) {
        Profilo profilo = getProfiloCorrente();
        if (profilo == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean isCfValid = cfValidator.validaCodiceFiscale(
                request.getCodiceFiscale(),
                request.getNome(),
                request.getCognome(),
                request.getSesso(),
                request.getDataNascita() != null ? request.getDataNascita().toString() : null,
                request.getCitta()
        );

        if (!isCfValid) {
            return ResponseEntity.badRequest().body("Il Codice Fiscale non corrisponde ai dati anagrafici forniti.");
        }

        profilo.setNome(Sauron.sanitize(request.getNome(), false));
        profilo.setCognome(Sauron.sanitize(request.getCognome(), false));
        profilo.setDataNascita(request.getDataNascita());
        profilo.setCodiceFiscale(request.getCodiceFiscale());
        profilo.setCitta(Sauron.sanitize(request.getCitta(), false));
        profilo.setIndirizzo(Sauron.sanitize(request.getIndirizzo(), false));
        profilo.setTelefono(Sauron.sanitize(request.getTelefono(), false));

        profiloDBMSBoundary.save(profilo);

        Map<String, Object> response = new HashMap<>();
        response.put("nome", profilo.getNome());
        response.put("cognome", profilo.getCognome());
        response.put("dataNascita", profilo.getDataNascita());
        response.put("codiceFiscale", profilo.getCodiceFiscale());
        response.put("citta", profilo.getCitta());
        response.put("indirizzo", profilo.getIndirizzo());
        response.put("telefono", profilo.getTelefono());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/other")
    public ResponseEntity<?> updateOther(@RequestBody OtherEditsDTO request) {
        Profilo profilo = getProfiloCorrente();
        if (profilo == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        profilo.setPolicyVisibilita(Sauron.sanitize(request.getPolicyVisibilita(), false));
        profilo.setDescrizione(Sauron.sanitize(request.getDescrizione(), false));
        profilo.setInteressi(Sauron.sanitize(request.getInteressi(), false));
        profilo.setCompetenze(Sauron.sanitize(request.getCompetenze(), false));

        profiloDBMSBoundary.save(profilo);

        Map<String, Object> response = new HashMap<>();
        response.put("policyVisibilita", profilo.getPolicyVisibilita());
        response.put("descrizione", profilo.getDescrizione());
        response.put("interessi", profilo.getInteressi());
        response.put("competenze", profilo.getCompetenze());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/credentials")
    public ResponseEntity<?> updateCredentials(@RequestBody CredentialsDTO request) {
        UtenteAfam utente = getUtenteCorrente();
        if (utente == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (request.getEmail() == null || !request.getEmail().matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")) {
            return ResponseEntity.badRequest().body("Formato email non valido");
        }
        
        request.setUsername(Sauron.sanitize(request.getUsername(), false));

        // Check if username/email are already used by someone else
        if (!utente.getUsername().equals(request.getUsername()) && 
            utenteAfamDBMSBoundary.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username già in uso");
        }
        
        if (!utente.getEmail().equals(request.getEmail()) && 
            utenteAfamDBMSBoundary.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email già in uso");
        }

        utente.setUsername(request.getUsername());
        utente.setEmail(request.getEmail());
        utenteAfamDBMSBoundary.save(utente);

        // Generate new JWT
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwt = jwtService.generateToken(userDetails);
        
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordDTO request) {
        UtenteAfam utente = getUtenteCorrente();
        if (utente == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (request.getNewPassword() == null || request.getNewPassword().length() < 12) {
            return ResponseEntity.badRequest().body("La nuova password deve contenere almeno 12 caratteri");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), utente.getPassword())) {
            return ResponseEntity.badRequest().body("La password attuale non è corretta");
        }

        utente.setPassword(passwordEncoder.encode(request.getNewPassword()));
        utenteAfamDBMSBoundary.save(utente);

        
        return ResponseEntity.ok("Credenziali aggiornate con successo. Effettua nuovamente il login.");
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicProfile(@PathVariable java.util.UUID id) {
        Optional<Profilo> pOpt = profiloDBMSBoundary.findById(id);
        if (pOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profilo non trovato.");
        }

        Profilo p = pOpt.get();
        // Cerca i contenuti pubblici associati a questo profilo.
        // Assumo che nel repository ContenutoDBMSBoundary ci sia un metodo per farlo.
        // Se non c'è, posso mappare solo il profilo e recuperare i contenuti in un altro momento,
        // ma preferisco preparare un map con i dettagli base.
        
        Map<String, Object> response = new HashMap<>();
        if (p.getNome() != null || p.getCognome() != null) {
            response.put("nome", p.getNome());
            response.put("cognome", p.getCognome());
            response.put("citta", p.getCitta());
            response.put("telefono", p.getTelefono());
        }
        response.put("id", p.getId());
        response.put("descrizione", p.getDescrizione());
        
        List<Contenuto> allContenuti = contenutoDBMSBoundary.findByProfilo(p);
        List<Contenuto> publicContenuti = new ArrayList<>();
        if (allContenuti != null) {
            for(Contenuto c : allContenuti) {
                if ("Pubblico".equalsIgnoreCase(c.getPolicyVisibilita())) {
                    publicContenuti.add(c);
                }
            }
        }
        response.put("contenuti", publicContenuti);
        
        return ResponseEntity.ok(response);
    }
}
