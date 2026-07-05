package com.afam.identity.middleware;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InputDetector {

    public static class InputDetectorResult {
        public String format;
        public String decoded;
        public boolean isEncoded;

        public InputDetectorResult(String format, String decoded, boolean isEncoded) {
            this.format = format;
            this.decoded = decoded;
            this.isEncoded = isEncoded;
        }
    }

    public static InputDetectorResult automatic_detective(String input) {
        if (input == null) return new InputDetectorResult("Unknown", input, false);
        String decoded = "";
        String lastFormat = "";
        String currentInput = input;

        while (true) {
            String match = JSON_detective(currentInput);
            if (match == null) match = URL_detective(currentInput);
            if (match == null) match = HEX_detective(currentInput);
            if (match == null) match = B64_detective(currentInput);
            if (match == null) match = BIN_detective(currentInput);

            if (match == null) {
                System.out.println("Nessuna codifica rilevata per l'input: \"" + currentInput + "\". Trattamento come testo semplice.");
                return new InputDetectorResult("Plain input", currentInput, !lastFormat.isEmpty());
            }

            System.out.println("Formato rilevato: " + match + " per l'input: \"" + currentInput + "\". Applicazione della decodifica ricorsiva.");

            switch (match) {
                case "JSON":
                    decoded = JSON_recoursed_decode(currentInput);
                    break;
                case "URL":
                    decoded = URL_recursive_decode(currentInput);
                    break;
                case "HEX":
                    decoded = HEX_recursive_decode(currentInput);
                    break;
                case "B64":
                    decoded = B64_recursive_decode(currentInput);
                    break;
                case "BIN":
                    decoded = BIN_recursive_decode(currentInput);
                    break;
                default:
                    System.out.println("Formato sconosciuto rilevato per l'input: \"" + currentInput + "\". Input eliminato per sicurezza.");
                    return new InputDetectorResult("Plain input", null, false);
            }

            if (decoded == null) return new InputDetectorResult("Plain input", currentInput, false);
            lastFormat = match;
            currentInput = decoded;
        }
    }

    public static String URL_detective(String input) {
        if (!input.contains("%")) return null;
        try {
            String decoded = URLDecoder.decode(input, StandardCharsets.UTF_8.name());
            if (!decoded.equals(input)) {
                return "URL";
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static String B64_detective(String input) {
        String cleanInput = input.replaceAll("\\s+", "");
        if (cleanInput.length() % 4 != 0 || cleanInput.length() == 0) return null;
        if (!cleanInput.matches("^[A-Za-z0-9+/]+={0,2}$")) return null;

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cleanInput);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);

            String reencoded = Base64.getEncoder().encodeToString(decoded.getBytes(StandardCharsets.UTF_8));
            if (!reencoded.equals(cleanInput)) {
                return null;
            }

            if (decoded.matches("^[\\x20-\\x7E\\u00A0-\\uFFFF\\n\\r\\t]*$") && !decoded.trim().isEmpty()) {
                if (cleanInput.length() < 32 && !Pattern.compile("[<>()\"']").matcher(decoded).find()) {
                    return null;
                }
                return "B64";
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static String HEX_detective(String input) {
        String cleanInput = input.replaceAll("\\s+", "");
        if (cleanInput.length() % 2 != 0 || !cleanInput.matches("^[0-9A-Fa-f]+$")) return null;

        try {
            byte[] bytes = new byte[cleanInput.length() / 2];
            for (int i = 0; i < cleanInput.length(); i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(cleanInput.substring(i, i + 2), 16);
            }
            String decoded = new String(bytes, StandardCharsets.UTF_8);

            if (decoded.contains("\uFFFD")) return null;

            if (decoded.matches("^[\\x20-\\x7E\\u00A0-\\uFFFF\\n\\r\\t]+$")) {
                if (cleanInput.length() < 32 && !Pattern.compile("[<>()\"']").matcher(decoded).find()) {
                    return null;
                }
                return "HEX";
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static String BIN_detective(String input) {
        String cleaninput = input.replaceAll("\\s+", "");
        if (cleaninput.length() == 0 || cleaninput.length() % 8 != 0 || !cleaninput.matches("^[01]+$")) return null;

        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cleaninput.length(); i += 8) {
                String b = cleaninput.substring(i, i + 8);
                sb.append((char) Integer.parseInt(b, 2));
            }
            String decoded = sb.toString();

            if (decoded.matches("^[\\x20-\\x7E\\u00A0-\\uFFFF\\n\\r\\t]+$")) {
                if (cleaninput.length() < 64 && !Pattern.compile("[<>()\"']").matcher(decoded).find()) {
                    return null;
                }
                return "BIN";
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static String JSON_detective(String input) {
        String trimmed = input.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(trimmed);
            return "JSON";
        } catch (Exception e) {
            return null;
        }
    }

    public static String B64_recursive_decode(String input) {
        String current = input;
        String decoded = "";
        Pattern b64Regex = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");

        try {
            while (!current.equals(decoded)) {
                if(decoded.length() > 0) current = decoded;
                if (!b64Regex.matcher(current).matches()) break;
                byte[] bytes = Base64.getDecoder().decode(current);
                decoded = new String(bytes, StandardCharsets.UTF_8);
            }
            if (b64Regex.matcher(current).matches() && current.length() > 4) {
                byte[] bytes = Base64.getDecoder().decode(current);
                String b64Decoded = new String(bytes, StandardCharsets.UTF_8);
                if (b64Decoded.matches("^[\\x20-\\x7E\\u00A0-\\uFFFF]*$")) {
                    current = b64Decoded;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return current;
    }

    public static String URL_recursive_decode(String input) {
        String current = input.replaceAll("\\s+", "");
        try {
            while (URL_detective(current) != null) {
                current = URLDecoder.decode(current, StandardCharsets.UTF_8.name());
            }
        } catch (Exception e) {
            return null;
        }
        return current;
    }

    public static String HEX_recursive_decode(String input) {
        String current = input.replaceAll("\\s+", "");
        try {
            while (HEX_detective(current) != null) {
                if (current.length() % 2 != 0) current = "0" + current;
                byte[] bytes = new byte[current.length() / 2];
                for (int i = 0; i < current.length(); i += 2) {
                    bytes[i / 2] = (byte) Integer.parseInt(current.substring(i, i + 2), 16);
                }
                current = new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            return null;
        }
        return current;
    }

    public static String BIN_recursive_decode(String input) {
        String current = input.replaceAll("\\s+", "");
        try {
            while (BIN_detective(current) != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < current.length(); i += 8) {
                    sb.append((char) Integer.parseInt(current.substring(i, i + 8), 2));
                }
                current = sb.toString();
            }
        } catch (Exception e) {
            return null;
        }
        return current;
    }

    public static String JSON_recoursed_decode(String input) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree = mapper.readTree(input);
            return tree.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
