const InputDetector = require("./InputDetector.js");
const connectionProtector = require("./ConnectionProtector.js");
// --- LOGICA DI SICUREZZA PWM (Cap. 14) ---

const Validator = {
  // Sanitizzazione ricorsiva (Cap 14.1)
  checker: (input) => {
    if (typeof input !== "string") return input;
    let InputDetector_res = InputDetector.automatic_detective(input);
    if (InputDetector_res.isEncoded) {
      console.log(
        `Input sospetto rilevato: "${input}". TIPO: ${InputDetector_res.encconnectionProtectorg}. Applicazione della sanitizzazione ricorsiva.`,
      );
      return InputDetector_res.decoded;
    } else {
      console.log(
        `Input non codificato rilevato: "${input}". Applicazione della sanitizzazione standard.`,
      );
    }
    return input;
  },

  sanitize: (input) => {
    if (typeof input !== "string") return input;
    let sanitized = input.normalize("NFC"); // Gestione Non-ASCII
    const dangerousChars = /[<>()"']/g; // Caratteri vietati dal doc
    console.log(`Sanitizzazione in corso. Input originale: "${input}"`);
    let previous;
    do {
      previous = sanitized;
      sanitized = sanitized.replace(dangerousChars, "");
    } while (sanitized !== previous);

    return sanitized;
  },

  // Parsing e Validazione (Cap 14.2)

  process: async (body, schema, result) => {
    for (let field in schema) {
      // Sanitizzazione di ogni campo in ingresso
      let rawValue = body[field] || "";
      let InputDetectorValue = Validator.checker(rawValue);
      let safeValue = Validator.sanitize(InputDetectorValue);
      let connectionProtectorResult = connectionProtector.ConnectionProtector_manager(safeValue);

      if (connectionProtectorResult === false) {
        result.isValid = false;
        result.errors.push(`Input bloccato da ConnectionProtector: "${safeValue}"`);
        safeValue = null; // Elimina il dato per sicurezza
        result.data[field] = null;
        continue; // Salta ulteriori controlli per questo campo
      } else {
        console.log(
          `Input passato attraverso ConnectionProtector: "${safeValue}". Risultato: ${connectionProtectorResult ? "Confermato" : "Bloccato"}.`,
        );
      }
      // Controllo integrità strutturale dopo la pulizia
      if (schema[field].required && !safeValue) {
        result.isValid = false;
        result.errors.push(
          `Campo ${field} mancante o svuotato dalla sanitizzazione`,
        );
        result.data[field] = null;
        continue;
      }

      if (
        typeof safeValue === "string" &&
        schema[field].minLength &&
        safeValue.length < schema[field].minLength
      ) {
        result.isValid = false;
        result.errors.push(
          `${field} troppo corta (minimo ${schema[field].minLength} caratteri)`,
        );
      }

      result.data[field] = safeValue;
    }
    return result;
  },

  guard: (schema, options = {}) => {
    const source = options.source || "body";
    return async (req, res, next) => {
      try {
        const payload = source === "query" ? req.query : req.body;
        const result = { isValid: true, data: {}, errors: [] };
        const processed = await Validator.process(payload, schema, result);

        if (!processed.isValid) {
          return res.status(400).json(processed);
        }

        req.safe = processed.data;
        return next();
      } catch (error) {
        console.error("Errore nel middleware Validator:", error);
        return res.status(500).json({
          error: "Errore interno del server",
          details: error.message,
        });
      }
    };
  },

  process_register: async (body) => {
    const schema = {
      username: { required: true },
      email: { required: true },
      password: { required: true, minLength: 12 },
    };
    let result = { isValid: true, data: {}, errors: [] };
    result = await Validator.process(body, schema, result);
    return result;
  },

  // Logica di Login
  process_login: async (body) => {
    const schema = {
      username: { required: true },
      password: { required: true, minLength: 12 },
    };
    let result = { isValid: true, data: {}, errors: [] };
    result = await Validator.process(body, schema, result);
    return result;
  },

  // Recupero username (email e password richieste)
  process_recovery_username: async (body) => {
    const schema = {
      email: { required: true },
      password: { required: true, minLength: 12 },
    };
    let result = { isValid: true, data: {}, errors: [] };
    result = await Validator.process(body, schema, result);
    return result;
  },

  // Recupero password: richiesta del link di reset via email
  process_recovery_password: async (body) => {
    const schema = {
      email: { required: true },
    };
    let result = { isValid: true, data: {}, errors: [] };
    result = await Validator.process(body, schema, result);
    return result;
  },
};

module.exports = Validator;
