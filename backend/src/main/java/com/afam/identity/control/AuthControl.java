package com.afam.identity.control;

import com.afam.identity.dto.AuthResponse;
import com.afam.identity.dto.LoginRequest;
import com.afam.identity.entity.Token;
import com.afam.identity.entity.UtenteAfam;
import com.afam.identity.boundary.TokenDBMSBoundary;
import com.afam.identity.boundary.UtenteAfamDBMSBoundary;
import com.afam.identity.service.JwtService;
import com.afam.identity.service.EmailService;
import com.afam.identity.dto.OtpRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthControl {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private UtenteAfamDBMSBoundary utenteAfamDBMSBoundary;
    
    @Autowired
    private TokenDBMSBoundary tokenDBMSBoundary;

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        Optional<UtenteAfam> utenteOpt = utenteAfamDBMSBoundary.findByUsername(request.getUsername());
        if (utenteOpt.isPresent()) {
            UtenteAfam utente = utenteOpt.get();
            if (Boolean.TRUE.equals(utente.getHas2fa())) {
                // Genera OTP e invia via email
                String otp = emailService.generateOtp();
                emailService.sendOtpEmail(utente.getEmail(), otp);

                Token otpToken = new Token();
                otpToken.setValore(otp);
                otpToken.setScadenza(LocalDateTime.now().plusMinutes(10));
                otpToken.setTipo("OTP_2FA");
                otpToken.setUtenteAfam(utente);
                tokenDBMSBoundary.save(otpToken);

                Map<String, String> response = new HashMap<>();
                response.put("status", "2FA_REQUIRED");
                response.put("username", request.getUsername());
                return ResponseEntity.ok(response);
            }
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "AFAM");
        final String jwt = jwtService.generateToken(extraClaims, userDetails);
        
        if (utenteOpt.isPresent()) {
            Token loginToken = new Token();
            loginToken.setValore(jwt);
            loginToken.setScadenza(LocalDateTime.now().plusDays(1));
            loginToken.setTipo("LOGIN");
            loginToken.setUtenteAfam(utenteOpt.get());
            loginToken.setAccessTime(LocalDateTime.now());
            tokenDBMSBoundary.save(loginToken);
        }
        
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/guest-login")
    public ResponseEntity<?> guestLogin() {
        final String jwt = jwtService.generateGuestToken();
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest request) {
        Optional<UtenteAfam> utenteOpt = utenteAfamDBMSBoundary.findByUsername(request.getUsername());
        if (utenteOpt.isPresent()) {
            UtenteAfam utente = utenteOpt.get();
            List<Token> tokens = tokenDBMSBoundary.findByUtenteAfamAndTipoAndValore(utente, "OTP_2FA", request.getOtp());
            
            for (Token t : tokens) {
                if (t.getScadenza().isAfter(LocalDateTime.now())) {
                    // OTP Valido, generiamo il JWT
                    tokenDBMSBoundary.delete(t); // Consuma l'OTPOffset

                    final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
                    Map<String, Object> extraClaims = new HashMap<>();
                    extraClaims.put("role", "AFAM");
                    final String jwt = jwtService.generateToken(extraClaims, userDetails);

                    Token loginToken = new Token();
                    loginToken.setValore(jwt);
                    loginToken.setScadenza(LocalDateTime.now().plusDays(1));
                    loginToken.setTipo("LOGIN");
                    loginToken.setUtenteAfam(utente);
                    loginToken.setAccessTime(LocalDateTime.now());
                    tokenDBMSBoundary.save(loginToken);

                    return ResponseEntity.ok(new AuthResponse(jwt));
                }
            }
        }
        return ResponseEntity.status(401).body("OTP non valido o scaduto");
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setup2fa(HttpServletRequest request) {
        // L'utente deve essere già autenticato con JWT per chiamare questa API
        String authHeader = request.getHeader("Authorization");
        String jwt = authHeader.substring(7);
        String username = jwtService.extractUsername(jwt);
        
        Optional<UtenteAfam> utenteOpt = utenteAfamDBMSBoundary.findByUsername(username);
        if (utenteOpt.isPresent()) {
            UtenteAfam utente = utenteOpt.get();
            String otp = emailService.generateOtp();
            emailService.sendOtpEmail(utente.getEmail(), otp);

            Token otpToken = new Token();
            otpToken.setValore(otp);
            otpToken.setScadenza(LocalDateTime.now().plusMinutes(10));
            otpToken.setTipo("OTP_2FA");
            otpToken.setUtenteAfam(utente);
            tokenDBMSBoundary.save(otpToken);

            return ResponseEntity.ok("OTP inviato via email per configurare la 2FA.");
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/2fa/confirm")
    public ResponseEntity<?> confirm2fa(@RequestBody OtpRequest otpRequest, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String jwt = authHeader.substring(7);
        String username = jwtService.extractUsername(jwt);

        Optional<UtenteAfam> utenteOpt = utenteAfamDBMSBoundary.findByUsername(username);
        if (utenteOpt.isPresent()) {
            UtenteAfam utente = utenteOpt.get();
            List<Token> tokens = tokenDBMSBoundary.findByUtenteAfamAndTipoAndValore(utente, "OTP_2FA", otpRequest.getOtp());

            for (Token t : tokens) {
                if (t.getScadenza().isAfter(LocalDateTime.now())) {
                    tokenDBMSBoundary.delete(t);
                    utente.setHas2fa(true);
                    utente.setMetodo2fa("EMAIL");
                    utenteAfamDBMSBoundary.save(utente);
                    return ResponseEntity.ok("2FA abilitata con successo.");
                }
            }
        }
        return ResponseEntity.status(400).body("OTP non valido o scaduto");
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String username = jwtService.extractUsername(jwt);
            Optional<UtenteAfam> utenteOpt = utenteAfamDBMSBoundary.findByUsername(username);
            
            if (utenteOpt.isPresent()) {
                Token blacklistedToken = new Token();
                blacklistedToken.setValore(jwt);
                // La scadenza è in LocalDateTime
                blacklistedToken.setScadenza(LocalDateTime.now().plusDays(1)); 
                blacklistedToken.setTipo("JWT_BLACKLIST");
                blacklistedToken.setUtenteAfam(utenteOpt.get());
                
                tokenDBMSBoundary.save(blacklistedToken);
            }
        }
        return ResponseEntity.ok("Logout completato");
    }
}
