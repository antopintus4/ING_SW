import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-edit-password-boundary',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './edit-password-boundary.component.html',
  styleUrl: './edit-password-boundary.component.css'
})
export class EditPasswordBoundaryComponent {
  passwords: any = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  errorMessage: string = '';
  successMessage: string = '';
  showOtpPanel: boolean = false;
  otpCode: string = '';

  constructor(private profileService: ProfileService, private router: Router) {}

  save() {
    if (this.passwords.newPassword.length < 12) {
      this.errorMessage = 'La password deve contenere almeno 12 caratteri.';
      return;
    }

    if (this.passwords.newPassword !== this.passwords.confirmPassword) {
      this.errorMessage = 'La nuova password e la conferma non coincidono.';
      return;
    }

    const payload = {
      currentPassword: this.passwords.currentPassword,
      newPassword: this.passwords.newPassword,
      otp: this.otpCode
    };

    this.profileService.updatePassword(payload).subscribe({
      next: (res: any) => {
        if (res && res.status === '2FA_REQUIRED') {
          this.showOtpPanel = true;
          this.successMessage = res.message;
          this.errorMessage = '';
        } else {
          this.successMessage = 'Password aggiornata con successo!';
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
          // Gestisce sia risposte map {error: ...} sia stringhe di errore semplici
          this.errorMessage = err.error?.error || err.error || 'Errore durante l\'aggiornamento della password.';
          this.successMessage = '';
        }
      }
    });
  }
}
