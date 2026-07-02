package com.afam.identity.control;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorControl {
    
    // Metodi scheletro per la gestione errori globale (caduta connessione DBMS, ecc.)
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public org.springframework.http.ResponseEntity<String> handleAuthenticationException(org.springframework.security.core.AuthenticationException e) {
        return org.springframework.http.ResponseEntity.status(401).body("Credenziali non valide");
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<String> handleError(Exception e) {
        e.printStackTrace();
        return org.springframework.http.ResponseEntity.status(500).body("Errore server: " + e.getMessage());
    }
}
