package com.afam.identity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Il tuo codice OTP per AFAM Identity");
        message.setText("Il tuo codice temporaneo è: " + otp);
        mailSender.send(message);
    }

    public void sendRecoveryEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Recupero Password AFAM Identity");
        message.setText("Clicca sul seguente link per reimpostare la tua password:\nhttp://localhost:4200/recovery/reset?token=" + token);
        mailSender.send(message);
    }

    public void sendUsernameRecoveryEmail(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Recupero Username AFAM Identity");
        message.setText("Hai richiesto un recupero email per l'account: " + username + "\nQuesta è l'email associata al tuo account.");
        mailSender.send(message);
    }

    public void sendDirectShareEmail(String toEmail, String linkCondivisione, String risorsa) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Qualcuno ha condiviso un contenuto con te");
        message.setText("Un utente ha condiviso la seguente risorsa con te: " + risorsa + "\nPuoi visualizzarla a questo link:\n" + linkCondivisione);
        mailSender.send(message);
    }
}
