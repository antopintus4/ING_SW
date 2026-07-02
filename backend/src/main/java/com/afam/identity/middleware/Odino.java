package com.afam.identity.middleware;

import java.util.Arrays;
import java.util.List;

public class Odino {
    private static final List<String> BLACKLIST = Arrays.asList("script", "php", "fetch", "curl");

    public static void detect_submission(String input) {}
    public static void trust_origin(String origin) {}
    public static void trust_calculator(String input) {}

    public static void redetect_submission(String referer) {}
    public static void refactor_submission(String input) {}

    public static void deny_submission(String input) {}
    
    public static boolean confirm_submission(String input) {
        System.out.println("Conferma della submission: \"" + input + "\".");
        return true;
    }

    public static void flow_analysis(String input) {}
    public static void flow_control(String input) {}

    public static void introspective_analysis(String input) {}
    public static void adaptive_response(String input) {}
    public static void feedback_loop(String input) {}
    public static void continuous_learning(String input) {}
    public static void threat_intelligence_integration(String input) {}
    public static void anomaly_detection(String input) {}
    public static void behavior_analysis(String input) {}
    public static void risk_assessment(String input) {}
    public static void dynamic_policy_enforcement(String input) {}
    public static void incident_response(String input) {}
    public static void post_incident_analysis(String input) {}

    public static void block_origin(String origin) {}
    
    public static boolean block_submission(String input) {
        System.out.println("Blocco della submission: \"" + input + "\".");
        return false;
    }
    
    public static void block_user(String user) {}
    public static void block_ip(String ip) {}
    public static void block_device(String device) {}
    public static void block_region(String region) {}
    public static void block_network(String network) {}

    public static boolean odino_manager(String input) {
        if (input == null) return true;
        
        boolean isBlocked = false;
        String blockedItem = null;
        String lowerInput = input.toLowerCase();

        for (String item : BLACKLIST) {
            if (lowerInput.contains(item.toLowerCase())) {
                isBlocked = true;
                blockedItem = item;
                break;
            }
        }

        if (isBlocked) {
            System.out.println("\"" + blockedItem + "\" - Input sospetto rilevato: \"" + input + "\". Applicazione del blocco.");
            return block_submission(input);
        } else {
            System.out.println("Input pulito rilevato: \"" + input + "\". Applicazione della conferma.");
            return confirm_submission(input);
        }
    }
}
