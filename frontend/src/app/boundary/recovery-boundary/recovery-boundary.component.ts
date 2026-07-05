import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { RecoveryService } from '../../services/recovery.service';

@Component({
  selector: 'app-recovery-boundary',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './recovery-boundary.component.html',
  styleUrl: './recovery-boundary.component.css'
})
export class RecoveryBoundaryComponent {
  email: string = '';
  username: string = '';
  mode: 'password' | 'email' = 'password';

  successMessage: string = '';
  errorMessage: string = '';

  constructor(private recoveryService: RecoveryService) {}

  switchMode(newMode: 'password' | 'email') {
    this.mode = newMode;
    this.successMessage = '';
    this.errorMessage = '';
  }

  requestPasswordRecovery() {
    this.recoveryService.requestPasswordRecovery(this.email).subscribe({
      next: (msg) => {
        this.successMessage = msg || "Email con le istruzioni per il recupero della password inviata.";
        this.errorMessage = '';
      },
      error: (err) => {
        this.errorMessage = err.error || "Questo indirizzo email non è associato a nessun account.";
        this.successMessage = '';
      }
    });
  }

  requestEmailRecovery() {
    this.recoveryService.requestEmailRecovery(this.username).subscribe({
      next: (res) => {
        this.successMessage = res.message || "E' stata inviata una email di notifica all'indirizzo email associato all'Username inserito.";
        this.errorMessage = '';
      },
      error: (err) => {
        this.errorMessage = err.error || "Nessun account associato all'Username inserito.";
        this.successMessage = '';
      }
    });
  }
}
