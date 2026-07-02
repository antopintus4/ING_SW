package com.afam.identity.middleware;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class Validator {

    public static String checker(String input) {
        if (input == null) return null;
        InputDetector.InputDetectorResult InputDetectorRes = InputDetector.automatic_detective(input);
        if (InputDetectorRes.isEncoded) {
            System.out.println("Input sospetto rilevato: \"" + input + "\". TIPO: " + InputDetectorRes.format + ". Applicazione della sanitizzazione ricorsiva.");
            return InputDetectorRes.decoded;
        } else {
            System.out.println("Input non codificato rilevato: \"" + input + "\". Applicazione della sanitizzazione standard.");
        }
        return input;
    }

    public static String sanitize(String input, boolean isEmail) {
        if (input == null) return null;
        
        String sanitized = Normalizer.normalize(input, Normalizer.Form.NFC);
        // Include RAD constraints and Validator script constraints
        // RAD: @ , ‘ , ’ , / , ; , - , “ , ” , #
        // Validator: < > ( ) " '
        String regex = isEmail ? "[<>()\"'‘’/;\\-“”#]" : "[<>()\"'‘’/;\\-“”#@]";
        
        System.out.println("Sanitizzazione in corso. Input originale: \"" + input + "\"");
        String previous;
        do {
            previous = sanitized;
            sanitized = sanitized.replaceAll(regex, "");
        } while (!sanitized.equals(previous));

        return sanitized;
    }

    public static List<String> sanitize(List<String> inputList, boolean isEmail) {
        if (inputList == null) return null;
        List<String> sanitizedList = new ArrayList<>();
        for (String item : inputList) {
            sanitizedList.add(sanitize(item, isEmail));
        }
        return sanitizedList;
    }

    public static class ProcessResult {
        public boolean isValid = true;
        public String data;
        public List<String> errors = new ArrayList<>();
    }

    public static ProcessResult processField(String input, boolean isEmail, boolean required, int minLength, String fieldName) {
        ProcessResult result = new ProcessResult();
        
        String rawValue = input != null ? input : "";
        String InputDetectorValue = checker(rawValue);
        String safeValue = sanitize(InputDetectorValue, isEmail);
        
        boolean odinResult = ConnectionProtector.ConnectionProtector_manager(safeValue);
        
        if (!odinResult) {
            result.isValid = false;
            result.errors.add("Input bloccato da ConnectionProtector: \"" + safeValue + "\"");
            safeValue = null;
        } else {
            System.out.println("Input passato attraverso ConnectionProtector: \"" + safeValue + "\". Risultato: Confermato.");
        }
        
        if (required && (safeValue == null || safeValue.isEmpty())) {
            result.isValid = false;
            result.errors.add("Campo " + fieldName + " mancante o svuotato dalla sanitizzazione");
            result.data = null;
            return result;
        }

        if (safeValue != null && minLength > 0 && safeValue.length() < minLength) {
            result.isValid = false;
            result.errors.add(fieldName + " troppo corta (minimo " + minLength + " caratteri)");
        }
        
        result.data = safeValue;
        return result;
    }
}
