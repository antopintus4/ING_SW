import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ProfileService } from '../../services/profile.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-edit-credentials-boundary',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './edit-credentials-boundary.component.html',
  styleUrl: './edit-credentials-boundary.component.css'
})
export class EditCredentialsBoundaryComponent implements OnInit {
  credentials: any = {
    username: '',
    email: ''
  };
  errorMessage: string = '';
  successMessage: string = '';
  showOtpPanel: boolean = false;
  otpCode: string = '';

  constructor(
    private profileService: ProfileService, 
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.profileService.getMe().subscribe({
      next: (data) => {
        if (data && data.utenteAfam) {
          this.credentials.username = data.utenteAfam.username || '';
          this.credentials.email = data.utenteAfam.email || '';
        }
      },
      error: () => this.errorMessage = 'Errore nel caricamento delle credenziali attuali.'
    });
  }

  save() {
    const payload = {
      username: this.credentials.username,
      email: this.credentials.email,
      otp: this.otpCode
    };

    this.profileService.updateCredentials(payload).subscribe({
      next: (res: any) => {
        if (res && res.status === '2FA_REQUIRED') {
          this.showOtpPanel = true;
          this.successMessage = res.message;
          this.errorMessage = '';
        } else {
          if (res && res.token) {
            this.authService.updateToken(res.token); // Rimpiazza il JWT con quello nuovo
          }
          this.successMessage = 'Credenziali aggiornate con successo!';
          this.errorMessage = '';
          this.showOtpPanel = false;
          setTimeout(() => this.router.navigate(['/profile']), 2000);
        }
      },
      error: (err) => {
        if (err.status === 400 && err.error === 'Errore nel token') {
          alert('Errore nel token');
          this.router.navigate(['/']);
        } else {
          this.errorMessage = err.error || 'Errore durante l\'aggiornamento delle credenziali.';
          this.successMessage = '';
        }
      }
    });
  }
}
