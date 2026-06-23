import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { RecoveryService } from '../../services/recovery.service';

@Component({
  selector: 'app-recovery-reset-boundary',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './recovery-reset-boundary.component.html',
  styleUrl: './recovery-reset-boundary.component.css'
})
export class RecoveryResetBoundaryComponent implements OnInit {
  token: string = '';
  newPassword: any = '';
  confirmPassword: any = '';
  
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private route: ActivatedRoute, 
    private recoveryService: RecoveryService,
    private router: Router
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.errorMessage = "Token mancante. Usa il link fornito via email.";
      }
    });
  }

  resetPassword() {
    if (!this.token) {
      this.errorMessage = "Token mancante. Impossibile procedere.";
      return;
    }
    
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = "Le password non coincidono.";
      return;
    }

    this.recoveryService.resetPassword(this.token, this.newPassword).subscribe({
      next: (msg) => {
        this.successMessage = msg || "Password modificata con successo.";
        this.errorMessage = '';
      },
      error: (err) => {
        this.errorMessage = err.error || "Token scaduto o non valido.";
        this.successMessage = '';
      }
    });
  }
}
