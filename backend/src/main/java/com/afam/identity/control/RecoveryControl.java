package com.afam.identity.control;

import com.afam.identity.boundary.TokenDBMSBoundary;
import com.afam.identity.boundary.UtenteAfamDBMSBoundary;
import com.afam.identity.entity.Token;
import com.afam.identity.entity.UtenteAfam;
import com.afam.identity.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/recovery")
@CrossOrigin(origins = "*")
public class RecoveryControl {
    
    @Autowired
    private UtenteAfamDBMSBoundary utenteAfamDBMSBoundary;
    
    @Autowired
    private TokenDBMSBoundary tokenDBMSBoundary;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/request")
    public ResponseEntity<?> requestRecovery(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<UtenteAfam> optUtente = utenteAfamDBMSBoundary.findByEmail(email);
        
        if (optUtente.isPresent()) {
            UtenteAfam utente = optUtente.get();
            
            Token token = new Token();
            token.setValore(UUID.randomUUID().toString());
            token.setScadenza(LocalDateTime.now().plusMinutes(15));
            token.setTipo("PWD_RECOVERY");
            token.setUtenteAfam(utente);
            
            tokenDBMSBoundary.save(token);
            emailService.sendRecoveryEmail(utente.getEmail(), token.getValore());
        }
        
        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Email con le istruzioni per il recupero della password inviata."));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String tokenVal = request.get("token");
        String newPassword = request.get("newPassword");
        
        Optional<Token> optToken = tokenDBMSBoundary.findByValore(tokenVal);
        if (optToken.isPresent()) {
            Token token = optToken.get();
            
            if (token.getScadenza().isBefore(LocalDateTime.now()) || !"PWD_RECOVERY".equals(token.getTipo())) {
                return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "Token scaduto o non valido."));
            }
            
            UtenteAfam utente = token.getUtenteAfam();
            utente.setPassword(passwordEncoder.encode(newPassword));
            utenteAfamDBMSBoundary.save(utente);
            
            // Invalida il token usandolo (eliminandolo)
            tokenDBMSBoundary.delete(token);
            
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Modifica della password avvenuta con successo."));
        }
        
        return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "Token non trovato."));
    }

    @PostMapping("/request-email")
    public ResponseEntity<?> requestEmailRecovery(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        Optional<UtenteAfam> optUtente = utenteAfamDBMSBoundary.findByUsername(username);

        if (optUtente.isPresent()) {
            UtenteAfam utente = optUtente.get();
            emailService.sendUsernameRecoveryEmail(utente.getEmail(), utente.getUsername());
            
            // Oscuramento parziale dell'email
            String email = utente.getEmail();
            int atIndex = email.indexOf("@");
            String obfuscatedEmail = "";
            if (atIndex > 1) {
                obfuscatedEmail = email.charAt(0) + "*****" + email.substring(atIndex);
            } else {
                obfuscatedEmail = "*****" + email.substring(atIndex);
            }
            
            return ResponseEntity.ok(Map.of("message", "E' stata inviata una email di notifica all'indirizzo email associato all'Username inserito: " + obfuscatedEmail));
        }
        
        return ResponseEntity.badRequest().body("Nessun account associato all'Username inserito");
    }
}
