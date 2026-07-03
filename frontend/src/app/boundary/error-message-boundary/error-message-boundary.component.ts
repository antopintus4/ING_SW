import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

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

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.errorType = params['type'] || 'unknown';
      this.setupErrorDetails();
    });
  }

  setupErrorDetails() {
    if (this.errorType === 'network') {
      this.errorMessage = "Assenza di Connettività: Impossibile raggiungere il server. Verifica la tua connessione o riprova più tardi.";
      this.errorIcon = "bi-wifi-off";
    } else if (this.errorType === 'server') {
      this.errorMessage = "Errore Interno del Server: Stiamo riscontrando problemi tecnici. I nostri tecnici sono al lavoro per risolvere il problema.";
      this.errorIcon = "bi-server";
    } else {
      this.errorMessage = "Si è verificato un errore imprevisto. Ti preghiamo di ritentare.";
      this.errorIcon = "bi-exclamation-triangle-fill";
    }
  }
}
