import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-profile-boundary',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile-boundary.component.html',
  styleUrl: './profile-boundary.component.css'
})
export class ProfileBoundaryComponent implements OnInit {
  setupStep: 'idle' | 'awaiting_otp' | 'success' = 'idle';
  otpCode: string = '';
  errorMessage: string = '';
  
  profilo: any = null;

  constructor(private authService: AuthService, private profileService: ProfileService) {}

  ngOnInit() {
    this.profileService.getMe().subscribe({
      next: (data) => this.profilo = data,
      error: () => this.errorMessage = 'Impossibile caricare i dati del profilo.'
    });
  }

  onSetup2fa() {
    this.errorMessage = '';
    this.authService.setup2fa().subscribe({
      next: () => {
        this.setupStep = 'awaiting_otp';
      },
      error: () => {
        this.errorMessage = 'Errore durante l\'invio dell\'email OTP.';
      }
    });
  }

  onConfirm2fa() {
    this.errorMessage = '';
    if (!this.otpCode || this.otpCode.length !== 6) {
      this.errorMessage = 'Inserisci un codice valido di 6 cifre.';
      return;
    }

    this.authService.confirm2fa(this.otpCode).subscribe({
      next: () => {
        this.setupStep = 'success';
      },
      error: () => {
        this.errorMessage = 'Codice errato o scaduto. Riprova.';
      }
    });
  }
}
