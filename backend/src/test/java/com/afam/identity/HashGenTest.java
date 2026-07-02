package com.afam.identity;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.time.LocalDate;
import com.afam.identity.control.CodiceFiscaleValidator;

public class HashGenTest {
    @Test
    public void generateHash() throws Exception {
        org.springframework.security.crypto.argon2.Argon2PasswordEncoder encoder = new org.springframework.security.crypto.argon2.Argon2PasswordEncoder(16, 32, 1, 1 << 14, 2);
        String rawPassword = "PasswordSicura123!";
        String encodedHash = encoder.encode(rawPassword);
        System.out.println("ENCODED: " + encodedHash);
        boolean matches = encoder.matches(rawPassword, encodedHash);
        System.out.println("ARGON2 MATCHES SELF: " + matches);
    }
}
