-- 1. UTENTE AFAM
-- Tabella principale per l'autenticazione.
CREATE TABLE utente_afam (
    uuid UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    has_2fa BOOLEAN DEFAULT FALSE,
    metodo_2fa VARCHAR(50),
    registration_with_identity_provider BOOLEAN DEFAULT FALSE,
    identity_provider VARCHAR(50)
);

-- 2. TOKEN
-- Collegata 1 a N con l'Utente (un utente può avere più token attivi/scaduti).
CREATE TABLE token (
    id UUID PRIMARY KEY,
    valore VARCHAR(255) NOT NULL UNIQUE,
    scadenza TIMESTAMP NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    utente_uuid UUID NOT NULL, -- Allineato a UUID per la Foreign Key
    access_time TIMESTAMP,
    FOREIGN KEY (utente_uuid) REFERENCES utente_afam(uuid) ON DELETE CASCADE
);

-- 3. PROFILO (con Anagrafica e Dati Accademici aggregati)
-- Relazione 1:1 con utente_afam (garantita dal vincolo UNIQUE sulla foreign key).
CREATE TABLE profilo (
    id UUID PRIMARY KEY,
    utente_uuid UUID NOT NULL UNIQUE,
    descrizione TEXT,
    policy_visibilita VARCHAR(50),   
    foto_profilo VARCHAR(255),
    -- Dati Anagrafici (Integrati per evitare JOIN inutili)
    nome VARCHAR(50) NOT NULL,
    cognome VARCHAR(50) NOT NULL,
    data_nascita DATE,
    codice_fiscale VARCHAR(16) UNIQUE,
    citta VARCHAR(100),
    indirizzo VARCHAR(255),
    telefono VARCHAR(20),
    
    -- Dati Accademici (Integrati)
    istituzione VARCHAR(100),
    dominio_istituzionale VARCHAR(100),
    matricola VARCHAR(50),
    corso_di_studi VARCHAR(100),
    anno_accademico VARCHAR(20),
    
    FOREIGN KEY (utente_uuid) REFERENCES utente_afam(uuid) ON DELETE CASCADE
);

CREATE TABLE profilo_interessi (
    profilo_id UUID NOT NULL,
    interesse VARCHAR(255) NOT NULL,
    PRIMARY KEY (profilo_id, interesse),
    FOREIGN KEY (profilo_id) REFERENCES profilo(id) ON DELETE CASCADE
);

CREATE TABLE profilo_competenze (
    profilo_id UUID NOT NULL,
    competenza VARCHAR(255) NOT NULL,
    PRIMARY KEY (profilo_id, competenza),
    FOREIGN KEY (profilo_id) REFERENCES profilo(id) ON DELETE CASCADE
);

-- 4. GRUPPO
-- Relazione dal 'portfolio' del profilo.
CREATE TABLE gruppo (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    profilo_id UUID NOT NULL, -- Il proprietario del gruppo
    FOREIGN KEY (profilo_id) REFERENCES profilo(id) ON DELETE CASCADE
);

-- 5. CONTENUTO
-- Relazione dal 'portfolio' del profilo.
CREATE TABLE contenuto (
    id UUID PRIMARY KEY,
    titolo VARCHAR(255) NOT NULL,
    tipo VARCHAR(50),
    descrizione TEXT,
    policy_visibilita VARCHAR(50),
    numero_visualizzazioni INT DEFAULT 0,
    profilo_id UUID NOT NULL, -- L'utente che ha creato il contenuto
    FOREIGN KEY (profilo_id) REFERENCES profilo(id) ON DELETE CASCADE
);

-- 6. ALLEGATO (Risolve l'attributo multivalore "allegati" di Contenuto)
CREATE TABLE allegato (
    id UUID PRIMARY KEY,
    contenuto_id UUID NOT NULL,
    url_file VARCHAR(255) NOT NULL,
    FOREIGN KEY (contenuto_id) REFERENCES contenuto(id) ON DELETE CASCADE
);

-- 7. TABELLE DI GIUNZIONE PER AUTORI E COLLABORATORI (Risolve attributi multivalore)
-- Permettono di associare più profili a un contenuto come autori o collaboratori.
CREATE TABLE contenuto_autore (
    contenuto_id UUID NOT NULL,
    nome_autore VARCHAR(255) NOT NULL,
    PRIMARY KEY (contenuto_id, nome_autore),
    FOREIGN KEY (contenuto_id) REFERENCES contenuto(id) ON DELETE CASCADE
);

CREATE TABLE contenuto_collaboratore (
    contenuto_id UUID NOT NULL,
    nome_collaboratore VARCHAR(255) NOT NULL,
    PRIMARY KEY (contenuto_id, nome_collaboratore),
    FOREIGN KEY (contenuto_id) REFERENCES contenuto(id) ON DELETE CASCADE
);

-- 8. TABELLA DI GIUNZIONE GRUPPO-CONTENUTO (Risolve la relazione N:M "aggrega")
CREATE TABLE gruppo_contenuto (
    gruppo_id UUID NOT NULL,
    contenuto_id UUID NOT NULL,
    PRIMARY KEY (gruppo_id, contenuto_id),
    FOREIGN KEY (gruppo_id) REFERENCES gruppo(id) ON DELETE CASCADE,
    FOREIGN KEY (contenuto_id) REFERENCES contenuto(id) ON DELETE CASCADE
);

-- 9. LINK (Gestione flessibile delle associazioni)
-- Le tre chiavi esterne sono NULLABLE. Solo una delle tre verrà popolata in base a cosa il link condivide.
CREATE TABLE link (
    identificatore_link VARCHAR(255) PRIMARY KEY,
    data_scadenza TIMESTAMP,
    risorsa_associata VARCHAR(255),
    stato VARCHAR(50),
    impostazioni TEXT, -- Può essere strutturato in JSON se il tuo DBMS lo supporta
    numero_visualizzazioni INT DEFAULT 0,
    
    -- Chiavi esterne (NULLABLE per supportare il collegamento a entità diverse)
    profilo_id UUID DEFAULT NULL,
    gruppo_id UUID DEFAULT NULL,
    contenuto_id UUID DEFAULT NULL,
    
    FOREIGN KEY (profilo_id) REFERENCES profilo(id) ON DELETE CASCADE,
    FOREIGN KEY (gruppo_id) REFERENCES gruppo(id) ON DELETE CASCADE,
    FOREIGN KEY (contenuto_id) REFERENCES contenuto(id) ON DELETE CASCADE
);