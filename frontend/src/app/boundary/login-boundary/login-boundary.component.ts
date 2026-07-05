import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login-boundary',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login-boundary.component.html',
  styleUrl: './login-boundary.component.css'
})
export class LoginBoundaryComponent {
  loginForm: FormGroup;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }


  onSubmit() {
    if (this.loginForm.invalid) return;

    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: (res: any) => {
        if (res.status === '2FA_REQUIRED') {
          // Passiamo lo username all'OTP component via state
          this.router.navigate(['/otp'], { state: { username: res.username } });
        } else {
          this.router.navigate(['/']);
        }
      },
      error: () => {
        this.errorMessage = 'Credenziali non valide o utente inesistente.';
      }
    });
  }

  onGuestLogin() {
    this.errorMessage = '';
    this.authService.guestLogin().subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: () => {
        this.errorMessage = 'Errore durante l\'accesso come visualizzatore.';
      }
    });
  }
}
