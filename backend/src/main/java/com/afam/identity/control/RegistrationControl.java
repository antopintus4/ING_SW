package com.afam.identity.control;

import com.afam.identity.dto.RegistrationRequest;
import com.afam.identity.entity.UtenteAfam;
import com.afam.identity.entity.Profilo;
import com.afam.identity.boundary.UtenteAfamDBMSBoundary;
import com.afam.identity.boundary.ProfiloDBMSBoundary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/registration")
public class RegistrationControl {
    
    @Autowired
    private UtenteAfamDBMSBoundary utenteAfamDBMSBoundary;
    
    @Autowired
    private ProfiloDBMSBoundary profiloDBMSBoundary;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CodiceFiscaleValidator cfValidator;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        if (utenteAfamDBMSBoundary.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username già in uso");
        }
        if (utenteAfamDBMSBoundary.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email già in uso");
        }

        // Validazione Codice Fiscale
        boolean isCfValid = cfValidator.validaCodiceFiscale(
                request.getCodiceFiscale(),
                request.getNome(),
                request.getCognome(),
                request.getSesso(),
                request.getDataNascita(),
                request.getCittaNascita()
        );

        if (!isCfValid) {
            return ResponseEntity.badRequest().body("Il Codice Fiscale non corrisponde ai dati anagrafici forniti.");
        }

        UtenteAfam utente = new UtenteAfam();
        utente.setUuid(UUID.randomUUID());
        utente.setUsername(request.getUsername());
        utente.setEmail(request.getEmail());
        utente.setPassword(passwordEncoder.encode(request.getPassword()));
        utente.setHas2fa(false);
        utente.setRegistrationWithIdentityProvider(false);

        UtenteAfam savedUtente = utenteAfamDBMSBoundary.save(utente);

        Profilo p = new Profilo();
        p.setUtenteAfam(savedUtente);
        p.setPolicyVisibilita("Privato");
        p.setNome(request.getNome());
        p.setCognome(request.getCognome());
        p.setCodiceFiscale(request.getCodiceFiscale());
        p.setDataNascita(request.getDataNascita() != null ? java.time.LocalDate.parse(request.getDataNascita()) : null);
        p.setCitta(request.getCittaNascita());

        profiloDBMSBoundary.save(p);

        return ResponseEntity.ok("Registrazione completata con successo");
    }
}
