import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 1 << 14, 2);
        String rawPassword = "PasswordSicura123!";
        String encodedHash = "$argon2id$v=19$m=16384,t=2,p=1$fh2zGjQxBSoiX1BBpEVI8g$/tj85XSiHIooMI1uDYceJ0tQHWV5d8I2p3pJdqufkgw";
        boolean matches = encoder.matches(rawPassword, encodedHash);
        System.out.println("Matches: " + matches);
    }
}
