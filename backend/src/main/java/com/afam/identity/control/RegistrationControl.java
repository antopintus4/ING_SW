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

import com.afam.identity.middleware.Validator;

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
        
        if (request.getEmail() == null || !request.getEmail().matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")) {
            return ResponseEntity.badRequest().body("Formato email non valido");
        }
        if (request.getPassword() == null || request.getPassword().length() < 12) {
            return ResponseEntity.badRequest().body("La password deve contenere almeno 12 caratteri");
        }

        request.setUsername(Validator.sanitize(request.getUsername(), false));
        request.setNome(Validator.sanitize(request.getNome(), false));
        request.setCognome(Validator.sanitize(request.getCognome(), false));
        request.setCittaNascita(Validator.sanitize(request.getCittaNascita(), false));
        request.setIstituzione(Validator.sanitize(request.getIstituzione(), false));
        request.setDominioIstituzionale(Validator.sanitize(request.getDominioIstituzionale(), false));
        request.setMatricola(Validator.sanitize(request.getMatricola(), false));
        request.setCorsoDiStudi(Validator.sanitize(request.getCorsoDiStudi(), false));
        request.setAnnoAccademico(Validator.sanitize(request.getAnnoAccademico(), false));

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
        p.setIstituzione(request.getIstituzione());
        p.setDominioIstituzionale(request.getDominioIstituzionale());
        p.setMatricola(request.getMatricola());
        p.setCorsoDiStudi(request.getCorsoDiStudi());
        p.setAnnoAccademico(request.getAnnoAccademico());

        profiloDBMSBoundary.save(p);

        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Registrazione completata con successo"));
    }
}
