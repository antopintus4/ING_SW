import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-otp-boundary',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './otp-boundary.component.html',
  styleUrl: './otp-boundary.component.css'
})
export class OtpBoundaryComponent implements OnInit {
  otpForm: FormGroup;
  errorMessage: string = '';
  username: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.otpForm = this.fb.group({
      otp: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]]
    });
    
    // Recupera lo username passato dal login component
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras.state && navigation.extras.state['username']) {
      this.username = navigation.extras.state['username'];
    }
  }

  ngOnInit() {
    if (!this.username) {
      // Se si accede a questa pagina direttamente senza username, torniamo al login
      this.router.navigate(['/login']);
    }
  }

  onSubmit() {
    if (this.otpForm.invalid) return;

    this.errorMessage = '';
    const otpValue = this.otpForm.get('otp')?.value;

    this.authService.verifyOtp(this.username, otpValue).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: () => {
        this.errorMessage = 'Codice OTP non valido o scaduto. Riprova.';
      }
    });
  }
}
