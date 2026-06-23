package com.afam.identity.control;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorControl {
    
    // Metodi scheletro per la gestione errori globale (caduta connessione DBMS, ecc.)
    @ExceptionHandler(Exception.class)
    public void handleError(Exception e) {
        // ...
    }
}
