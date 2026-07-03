$ErrorActionPreference = "Stop"

Write-Host "Verifica delle dipendenze in corso..." -ForegroundColor Green

# Check Java
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "Errore: Java non è installato o non è nel PATH." -ForegroundColor Red
    exit 1
}

# Check Node.js
if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    Write-Host "Errore: Node.js non è installato o non è nel PATH." -ForegroundColor Red
    exit 1
}

# Check npm
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Host "Errore: npm non è installato o non è nel PATH." -ForegroundColor Red
    exit 1
}

Write-Host "Tutte le dipendenze principali sono soddisfatte!" -ForegroundColor Green
Write-Host "--------------------------------------------------------"

$PROJECT_ROOT = Get-Location
$BACKEND_DIR = "$PROJECT_ROOT\backend"
$FRONTEND_DIR = "$PROJECT_ROOT\frontend"

Write-Host "Avvio del Backend (Spring Boot)..." -ForegroundColor Green
Set-Location $BACKEND_DIR

if (Test-Path "mvnw.cmd") {
    $BackendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c mvnw.cmd spring-boot:run" -PassThru
} else {
    $BackendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c mvn spring-boot:run" -PassThru
}


Write-Host "Controllo dipendenze e avvio del Frontend (Angular)..." -ForegroundColor Green
Set-Location $FRONTEND_DIR

Write-Host "Controllo e installazione dipendenze Angular in corso..."
npm install

$FrontendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c npm start" -PassThru

Write-Host "--------------------------------------------------------"
Write-Host "Backend e Frontend avviati con successo in nuove finestre!" -ForegroundColor Green
Write-Host "--------------------------------------------------------"
