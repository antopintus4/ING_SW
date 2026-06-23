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
    this.profileService.updateCredentials(this.credentials).subscribe({
      next: (res) => {
        if (res && res.token) {
          this.authService.updateToken(res.token); // Rimpiazza il JWT con quello nuovo
        }
        this.successMessage = 'Credenziali aggiornate con successo!';
        setTimeout(() => this.router.navigate(['/profile']), 2000);
      },
      error: (err) => {
        this.errorMessage = err.error || 'Errore durante l\'aggiornamento delle credenziali.';
      }
    });
  }
}
