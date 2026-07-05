package com.afam.identity.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicInteger;

@ControllerAdvice
public class ErrorControl {
    
    private static final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    @Autowired
    private DataSource dataSource;

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public org.springframework.http.ResponseEntity<String> handleAuthenticationException(org.springframework.security.core.AuthenticationException e) {
        consecutiveFailures.set(0); // Reset
        return org.springframework.http.ResponseEntity.status(401).body("Credenziali non valide");
    }

    private boolean isConnectionError(Throwable t) {
        if (t == null) return false;
        String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";
        if (t instanceof java.sql.SQLException || 
            t instanceof org.hibernate.exception.JDBCConnectionException ||
            t instanceof org.springframework.dao.DataAccessResourceFailureException ||
            msg.contains("connection") || msg.contains("communications link") || msg.contains("database is down") || msg.contains("timeout")) {
            return true;
        }
        return isConnectionError(t.getCause());
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<String> handleError(Exception e) {
        if (isConnectionError(e)) {
            int count = consecutiveFailures.incrementAndGet();
            System.err.println("DB Connection Failure detected! Consecutive failures count: " + count);
            
            if (count >= 5) {
                System.err.println("5 consecutive connection failures reached. Performing clean restart of connection pool...");
                try {
                    if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                        com.zaxxer.hikari.HikariDataSource ds = (com.zaxxer.hikari.HikariDataSource) dataSource;
                        if (ds.getHikariPoolMXBean() != null) {
                            ds.getHikariPoolMXBean().softEvictConnections();
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to soft evict connection pool: " + ex.getMessage());
                }
                consecutiveFailures.set(0); // Reset
            }
            
            return org.springframework.http.ResponseEntity.status(503).body("DBMS Offline: Errore di connessione al database.");
        }

        consecutiveFailures.set(0); // Reset on non-connection errors
        e.printStackTrace();
        return org.springframework.http.ResponseEntity.status(500).body("Errore server: " + e.getMessage());
    }
}
