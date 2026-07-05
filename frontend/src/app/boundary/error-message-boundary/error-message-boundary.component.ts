import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-error-message-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './error-message-boundary.component.html',
  styleUrl: './error-message-boundary.component.css'
})
export class ErrorMessageBoundaryComponent implements OnInit {
  errorType: string = '';
  errorMessage: string = '';
  errorIcon: string = '';
  redirectUrl: string = '/';

  // Stato Riconnessione
  isReconnecting: boolean = false;
  reconnectAttempts: number = 0;
  maxAttempts: number = 5;
  currentDelay: number = 0;
  reconnectSuccess: boolean | null = null; // null = non iniziato, true = successo, false = fallito
  baseX: number = 0; // valore di base 'x' generato alla prima iterazione (200-5000ms)

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.errorType = params['type'] || 'unknown';
      this.redirectUrl = params['redirectUrl'] || '/';
      this.setupErrorDetails();
    });
  }

  setupErrorDetails() {
    if (this.errorType === 'network') {
      this.errorMessage = "Errore di connessione: il server o il database non sono raggiungibili.";
      this.errorIcon = "bi-wifi-off";
    } else if (this.errorType === 'server') {
      this.errorMessage = "Errore Interno del Server: Stiamo riscontrando problemi tecnici.";
      this.errorIcon = "bi-server";
    } else {
      this.errorMessage = "Si è verificato un errore imprevisto. Ti preghiamo di ritentare.";
      this.errorIcon = "bi-exclamation-triangle-fill";
    }
  }

  async startReconnectionFlow() {
    this.isReconnecting = true;
    this.reconnectAttempts = 0;
    this.reconnectSuccess = null;
    
    // Genera x alla prima iterazione tra 200 ms e 5000 ms
    this.baseX = Math.floor(Math.random() * (5000 - 200 + 1)) + 200;
    console.log(`Avvio tentativi riconnessione. Valore base x: ${this.baseX}ms`);

    for (let i = 1; i <= this.maxAttempts; i++) {
      this.reconnectAttempts = i;
      
      try {
        // Step 3.1: Contatta il DBMS verificando lo stato del Database
        await firstValueFrom(this.http.get('/api/auth/ping', { responseType: 'text' }));
        
        // Risposta positiva: interrompi la verifica (break)
        this.reconnectSuccess = true;
        break;
      } catch (err: any) {
        if (err.status && err.status !== 0 && err.status !== 503) {
          // Il server risponde con un codice valido diverso da errore di rete o DBMS offline
          this.reconnectSuccess = true;
          break;
        }

        // Attendi tempo casuale quadratico prima di ritentare
        if (i < this.maxAttempts) {
          this.currentDelay = this.baseX * Math.pow(i, 2);
          console.log(`Tentativo ${i} fallito. Attesa di ${this.currentDelay}ms...`);
          await new Promise(resolve => setTimeout(resolve, this.currentDelay));
        }
      }
    }

    this.isReconnecting = false;
    
    // Step 5: Se falliti tutti i 5 tentativi
    if (this.reconnectSuccess !== true) {
      this.reconnectSuccess = false;
      this.errorMessage = "Errore di sistema durante la connessione.";
      this.errorIcon = "bi-cloud-slash";
    }
  }

  confirmRecovery() {
    this.router.navigateByUrl(this.redirectUrl);
  }

  restartApp() {
    window.location.reload();
  }
}
