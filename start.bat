@echo off
setlocal

echo Verifica delle dipendenze in corso...

:: Check Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRORE] Java non e' installato o non e' nel PATH.
    exit /b 1
)

:: Check Node.js
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRORE] Node.js non e' installato o non e' nel PATH.
    exit /b 1
)

:: Check npm
where npm >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRORE] npm non e' installato o non e' nel PATH.
    exit /b 1
)

echo Tutte le dipendenze principali sono soddisfatte!
echo --------------------------------------------------------

set "PROJECT_ROOT=%~dp0"
set "BACKEND_DIR=%PROJECT_ROOT%backend"
set "FRONTEND_DIR=%PROJECT_ROOT%frontend"

:: Start Database via Docker
echo Avvio del Database tramite Docker...
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [AVVISO] Docker non e' installato o non e' nel PATH. Impossibile avviare il container del database in automatico.
) else (
    cd /d "%PROJECT_ROOT%"
    echo Avvio del container 'database'...
    docker compose up -d database
    :: Attendi qualche secondo per permettere al DB di inizializzarsi
    timeout /t 5 /nobreak >nul
)

:: Start Backend
echo Avvio del Backend (Spring Boot)...
if not exist "%BACKEND_DIR%" (
    echo [ERRORE] Directory backend non trovata!
    exit /b 1
)

cd /d "%BACKEND_DIR%"
if exist mvnw.cmd (
    start "AFAM Backend" cmd /k "mvnw.cmd spring-boot:run"
) else (
    start "AFAM Backend" cmd /k "mvn spring-boot:run"
)

:: Start Frontend
echo Controllo dipendenze e avvio del Frontend (Angular)...
if not exist "%FRONTEND_DIR%" (
    echo [ERRORE] Directory frontend non trovata!
    exit /b 1
)

cd /d "%FRONTEND_DIR%"
echo Controllo e installazione dipendenze Angular in corso...
call npm install --ignore-scripts

start "AFAM Frontend" cmd /k "npx ng serve --proxy-config proxy.conf.json"

echo --------------------------------------------------------
echo Backend e Frontend avviati con successo in finestre separate!
echo Chiudi le rispettive finestre per arrestare i servizi.
echo --------------------------------------------------------

cd /d "%PROJECT_ROOT%"
