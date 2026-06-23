package com.afam.identity.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class EmailService {

    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        // Mock dell'invio email: in un ambiente di produzione qui si userà JavaMailSender
        System.out.println("==========================================================");
        System.out.println("MOCK EMAIL SENDER - INVIATO A: " + toEmail);
        System.out.println("Oggetto: Il tuo codice OTP per AFAM Identity");
        System.out.println("Messaggio: Il tuo codice temporaneo è: " + otp);
        System.out.println("==========================================================");
    }

    public void sendRecoveryEmail(String toEmail, String token) {
        System.out.println("==========================================================");
        System.out.println("MOCK EMAIL SENDER - INVIATO A: " + toEmail);
        System.out.println("Oggetto: Recupero Password AFAM Identity");
        System.out.println("Messaggio: Clicca sul seguente link per reimpostare la tua password:");
        System.out.println("http://localhost:4200/recovery/reset?token=" + token);
        System.out.println("==========================================================");
    }

    public void sendUsernameRecoveryEmail(String toEmail, String username) {
        System.out.println("==========================================================");
        System.out.println("MOCK EMAIL SENDER - INVIATO A: " + toEmail);
        System.out.println("Oggetto: Recupero Email AFAM Identity");
        System.out.println("Messaggio: Hai richiesto un recupero email per l'account: " + username);
        System.out.println("Questa è l'email associata al tuo account.");
        System.out.println("==========================================================");
    }
}
