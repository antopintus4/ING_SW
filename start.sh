#!/bin/bash

set -o pipefail

# Define colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BACKEND_HOST="localhost"
BACKEND_PORT="8080"
DB_NAME="afam_identity"
DB_USER="postgres"
DB_PASSWORD="password"

echo -e "${GREEN}Verifica delle dipendenze in corso...${NC}"

# Check Java
if ! java -version &> /dev/null; then
    echo -e "${RED}Errore: Java non è installato o non è nel PATH.${NC}"
    exit 1
fi

# Check Node.js
if ! node -v &> /dev/null; then
    echo -e "${RED}Errore: Node.js non è installato o non è nel PATH.${NC}"
    exit 1
fi

# Check npm
if ! npm -v &> /dev/null; then
    echo -e "${RED}Errore: npm non è installato o non è nel PATH.${NC}"
    exit 1
fi

echo -e "${GREEN}Tutte le dipendenze principali sono soddisfatte!${NC}"
echo "--------------------------------------------------------"

# Directory paths
PROJECT_ROOT=$(pwd)
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

# Function to clean up background processes on exit
cleanup() {
    echo -e "\n${RED}Arresto dei servizi...${NC}"
    # Kill the entire process group for backend and frontend if possible, otherwise just PIDs
    kill ${BACKEND_PID:-} ${FRONTEND_PID:-} 2>/dev/null
    exit
}

# Trap SIGINT and SIGTERM to run cleanup function
trap cleanup SIGINT SIGTERM

# Start Backend
echo -e "${GREEN}Avvio del Backend (Spring Boot)...${NC}"
cd "$BACKEND_DIR" || { echo -e "${RED}Directory backend non trovata!${NC}"; exit 1; }

if ! command -v psql >/dev/null 2>&1; then
    echo -e "${RED}Errore: psql non è installato o non è nel PATH.${NC}"
    exit 1
fi

echo "Allineamento dello schema del database in corso..."
if ! PGPASSWORD="$DB_PASSWORD" psql -h "$BACKEND_HOST" -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
ALTER TABLE contenuto
    ADD COLUMN IF NOT EXISTS data_caricamento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
SQL
then
    echo -e "${RED}Errore: impossibile allineare lo schema del database.${NC}"
    exit 1
fi

# Make mvnw executable if it exists
if [ -f "mvnw" ] && [ ! -x "mvnw" ]; then
    chmod +x mvnw
fi

# Run the backend
if [ -f "mvnw" ]; then
    ./mvnw spring-boot:run &
else
    mvn spring-boot:run &
fi
BACKEND_PID=$!

wait_for_backend_startup() {
    local timeout_seconds=90
    local elapsed_seconds=0

    while [ "$elapsed_seconds" -lt "$timeout_seconds" ]; do
        if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
            wait "$BACKEND_PID"
            return 1
        fi

        if exec 3<>/dev/tcp/$BACKEND_HOST/$BACKEND_PORT 2>/dev/null; then
            exec 3>&-
            exec 3<&-
            return 0
        fi

        sleep 1
        elapsed_seconds=$((elapsed_seconds + 1))
    done

    echo -e "${RED}Errore: il Backend non ha completato l'avvio entro ${timeout_seconds}s.${NC}"
    return 1
}

if ! wait_for_backend_startup; then
    cleanup
fi

# Start Frontend
echo -e "${GREEN}Controllo dipendenze e avvio del Frontend (Angular)...${NC}"
cd "$FRONTEND_DIR" || { echo -e "${RED}Directory frontend non trovata!${NC}"; exit 1; }

# Install/update Angular dependencies (npm install runs quickly if already up to date)
echo "Controllo e installazione dipendenze Angular in corso..."
if ! npm install; then
    echo -e "${RED}Errore: impossibile installare o aggiornare le dipendenze Angular.${NC}"
    cleanup
fi


# Start frontend
npm start &
FRONTEND_PID=$!

echo "--------------------------------------------------------"
echo -e "${GREEN}Backend avviato correttamente e Frontend in esecuzione!${NC}"
echo -e "Premi ${RED}Ctrl+C${NC} per arrestare entrambi i servizi."
echo "--------------------------------------------------------"

# Wait for background processes to keep the script running
wait $BACKEND_PID $FRONTEND_PID
