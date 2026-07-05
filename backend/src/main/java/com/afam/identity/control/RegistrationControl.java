package com.afam.identity.control;

import com.afam.identity.dto.RegistrationRequest;
import com.afam.identity.entity.UtenteAfam;
import com.afam.identity.entity.Profilo;
import com.afam.identity.boundary.UtenteAfamDBMSBoundary;
import com.afam.identity.boundary.ProfiloDBMSBoundary;
import com.afam.identity.boundary.TokenDBMSBoundary;
import com.afam.identity.entity.Token;
import com.afam.identity.service.EmailService;
import com.afam.identity.dto.OtpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.Optional;

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

    @Autowired
    private TokenDBMSBoundary tokenDBMSBoundary;

    @Autowired
    private EmailService emailService;

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

        // Genera OTP e salva come token di attivazione
        String otp = emailService.generateOtp();
        Token regToken = new Token();
        regToken.setValore(otp);
        regToken.setScadenza(LocalDateTime.now().plusMinutes(10));
        regToken.setTipo("registrazione");
        regToken.setUtenteAfam(savedUtente);
        tokenDBMSBoundary.save(regToken);

        emailService.sendRegistrationOtpEmail(savedUtente.getEmail(), otp);

        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Registrazione profilo avvenuta con successo. Controllare la mail per confermare l'account."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest request) {
        Optional<UtenteAfam> utenteOpt = utenteAfamDBMSBoundary.findByUsername(request.getUsername());
        if (utenteOpt.isPresent()) {
            UtenteAfam utente = utenteOpt.get();
            List<Token> tokens = tokenDBMSBoundary.findByUtenteAfamAndTipoAndValore(utente, "registrazione", request.getOtp());
            
            for (Token t : tokens) {
                if (t.getScadenza().isAfter(LocalDateTime.now())) {
                    tokenDBMSBoundary.delete(t); // Consuma il token
                    return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Registrazione completata e verificata con successo."));
                }
            }
        }
        return ResponseEntity.status(400).body("Codice di verifica non valido o scaduto.");
    }
}
