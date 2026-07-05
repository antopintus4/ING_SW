package com.afam.identity.control;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CodiceFiscaleValidator {

    private List<Map<String, Object>> comuni;

    public CodiceFiscaleValidator() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = new ClassPathResource("comuni.json").getInputStream();
            comuni = mapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento dei comuni: " + e.getMessage());
        }
    }

    public boolean validaCodiceFiscale(String cf, String nome, String cognome, String sesso, String dataNascitaStr, String citta) {
        if (cf == null || cf.length() != 16) return false;
        if (nome == null || cognome == null || sesso == null || dataNascitaStr == null || citta == null) return false;

        cf = cf.toUpperCase().trim();
        nome = nome.toUpperCase().trim();
        cognome = cognome.toUpperCase().trim();
        sesso = sesso.toUpperCase().trim();
        citta = citta.trim();

        try {
            LocalDate dataNascita = LocalDate.parse(dataNascitaStr, DateTimeFormatter.ISO_LOCAL_DATE);
            String cfCalcolato = calcolaCF(nome, cognome, sesso, dataNascita, citta);
            return cf.equals(cfCalcolato);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String calcolaCF(String nome, String cognome, String sesso, LocalDate data, String citta) {
        StringBuilder cf = new StringBuilder();

        cf.append(calcolaCognome(cognome));
        cf.append(calcolaNome(nome));
        cf.append(String.format("%02d", data.getYear() % 100));

        char[] mesi = {'A', 'B', 'C', 'D', 'E', 'H', 'L', 'M', 'P', 'R', 'S', 'T'};
        cf.append(mesi[data.getMonthValue() - 1]);

        int giorno = data.getDayOfMonth();
        if ("F".equals(sesso)) {
            giorno += 40;
        }
        cf.append(String.format("%02d", giorno));

        String codiceCatastale = trovaCodiceCatastale(citta);
        if (codiceCatastale == null) {
            throw new RuntimeException("Città non trovata: " + citta);
        }
        cf.append(codiceCatastale);

        cf.append(calcolaCarattereControllo(cf.toString()));

        return cf.toString();
    }

    private String trovaCodiceCatastale(String citta) {
        if (comuni == null) return null;
        for (Map<String, Object> comune : comuni) {
            String nomeComune = (String) comune.get("nome");
            if (nomeComune != null && nomeComune.equalsIgnoreCase(citta)) {
                return (String) comune.get("codiceCatastale");
            }
        }
        return null;
    }

    private String calcolaCognome(String c) {
        String cons = c.replaceAll("[^A-Z]", "").replaceAll("[AEIOU]", "");
        String voc = c.replaceAll("[^A-Z]", "").replaceAll("[^AEIOU]", "");
        String res = cons + voc + "XXX";
        return res.substring(0, 3);
    }

    private String calcolaNome(String n) {
        String cons = n.replaceAll("[^A-Z]", "").replaceAll("[AEIOU]", "");
        String voc = n.replaceAll("[^A-Z]", "").replaceAll("[^AEIOU]", "");
        if (cons.length() >= 4) {
            return "" + cons.charAt(0) + cons.charAt(2) + cons.charAt(3);
        }
        String res = cons + voc + "XXX";
        return res.substring(0, 3);
    }

    private char calcolaCarattereControllo(String parziale) {
        int[] pesiDispari = {1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20, 11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23};
        int somma = 0;
        for (int i = 0; i < 15; i++) {
            char c = parziale.charAt(i);
            int v = Character.isDigit(c) ? (c - '0') : (c - 'A');
            if (i % 2 == 0) { // Dispari (1-based)
                somma += pesiDispari[v];
            } else { // Pari (1-based)
                somma += v;
            }
        }
        return (char) ('A' + (somma % 26));
    }

    public String generaCodiceFiscale(String nome, String cognome, String sesso, LocalDate dataNascita, String citta) {
        if (nome == null || cognome == null || sesso == null || dataNascita == null || citta == null) {
            return null;
        }
        try {
            return calcolaCF(nome.toUpperCase().trim(), cognome.toUpperCase().trim(), sesso.toUpperCase().trim(), dataNascita, citta.trim());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class EstrazioneCFResult {
        public LocalDate dataNascita;
        public String citta;
        public String sesso;
    }

    public EstrazioneCFResult estraiDatiDaCF(String cf) {
        if (cf == null || cf.length() != 16) return null;
        cf = cf.toUpperCase().trim();
        
        try {
            // Estrai Anno
            int annoYY = Integer.parseInt(cf.substring(6, 8));
            int currentYearYY = LocalDate.now().getYear() % 100;
            int annoYYYY = (annoYY <= currentYearYY) ? (2000 + annoYY) : (1900 + annoYY);

            // Estrai Mese
            char meseChar = cf.charAt(8);
            int mese = 0;
            char[] mesi = {'A', 'B', 'C', 'D', 'E', 'H', 'L', 'M', 'P', 'R', 'S', 'T'};
            for (int i = 0; i < mesi.length; i++) {
                if (mesi[i] == meseChar) {
                    mese = i + 1;
                    break;
                }
            }
            if (mese == 0) return null; // Mese non valido

            // Estrai Giorno e Sesso
            int giornoVal = Integer.parseInt(cf.substring(9, 11));
            String sesso = "M";
            int giorno = giornoVal;
            if (giornoVal > 40) {
                sesso = "F";
                giorno = giornoVal - 40;
            }
            
            LocalDate dataNascita = LocalDate.of(annoYYYY, mese, giorno);

            // Estrai Città tramite Codice Catastale
            String codiceCatastale = cf.substring(11, 15);
            String citta = null;
            if (comuni != null) {
                for (Map<String, Object> comune : comuni) {
                    String cod = (String) comune.get("codiceCatastale");
                    if (cod != null && cod.equalsIgnoreCase(codiceCatastale)) {
                        citta = (String) comune.get("nome");
                        break;
                    }
                }
            }

            EstrazioneCFResult res = new EstrazioneCFResult();
            res.dataNascita = dataNascita;
            res.citta = citta;
            res.sesso = sesso;
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
