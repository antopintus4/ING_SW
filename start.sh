#!/bin/bash

# Define colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Verifica delle dipendenze in corso...${NC}"

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}Errore: Java non è installato o non è nel PATH.${NC}"
    exit 1
fi

# Check Node.js
if ! command -v node &> /dev/null; then
    echo -e "${RED}Errore: Node.js non è installato o non è nel PATH.${NC}"
    exit 1
fi

# Check npm
if ! command -v npm &> /dev/null; then
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
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
    exit
}

# Trap SIGINT and SIGTERM to run cleanup function
trap cleanup SIGINT SIGTERM

# Start Backend
echo -e "${GREEN}Avvio del Backend (Spring Boot)...${NC}"
cd "$BACKEND_DIR" || { echo -e "${RED}Directory backend non trovata!${NC}"; exit 1; }

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

# Start Frontend
echo -e "${GREEN}Controllo dipendenze e avvio del Frontend (Angular)...${NC}"
cd "$FRONTEND_DIR" || { echo -e "${RED}Directory frontend non trovata!${NC}"; exit 1; }

# Install/update Angular dependencies (npm install runs quickly if already up to date)
echo "Controllo e installazione dipendenze Angular in corso..."
npm install


# Start frontend
npm start &
FRONTEND_PID=$!

echo "--------------------------------------------------------"
echo -e "${GREEN}Backend e Frontend avviati con successo!${NC}"
echo -e "Premi ${RED}Ctrl+C${NC} per arrestare entrambi i servizi."
echo "--------------------------------------------------------"

# Wait for background processes to keep the script running
wait $BACKEND_PID $FRONTEND_PID
