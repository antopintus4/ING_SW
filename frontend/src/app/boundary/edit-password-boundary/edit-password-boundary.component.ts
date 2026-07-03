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

    this.profileService.updatePassword(this.passwords).subscribe({
      next: () => {
        this.successMessage = 'Password aggiornata con successo!';
        this.errorMessage = '';
        setTimeout(() => this.router.navigate(['/profile']), 2000);
      },
      error: (err) => {
        this.errorMessage = err.error || 'Errore durante l\'aggiornamento della password.';
        this.successMessage = '';
      }
    });
  }
}
