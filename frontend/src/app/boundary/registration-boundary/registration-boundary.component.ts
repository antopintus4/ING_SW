import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import CodiceFiscale from 'codice-fiscale-js';

@Component({
  selector: 'app-registration-boundary',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './registration-boundary.component.html',
  styleUrl: './registration-boundary.component.css'
})
export class RegistrationBoundaryComponent {
  registerForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      nome: ['', Validators.required],
      cognome: ['', Validators.required],
      sesso: ['', Validators.required],
      dataNascita: ['', Validators.required],
      cittaNascita: ['', Validators.required],
      codiceFiscale: ['', [Validators.required, Validators.pattern('^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$')]]
    });
  }

  onSubmit() {
    if (this.registerForm.invalid) return;

    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register(this.registerForm.value).subscribe({
      next: (res) => {
        this.successMessage = res || 'Registrazione completata! Ora puoi effettuare il login.';
        this.registerForm.reset();
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.errorMessage = err.error || 'Errore durante la registrazione. Riprova.';
      }
    });
  }

  generaCF() {
    const vals = this.registerForm.value;
    if (!vals.nome || !vals.cognome || !vals.sesso || !vals.dataNascita || !vals.cittaNascita) {
      this.errorMessage = 'Compila nome, cognome, sesso, data e città di nascita per generare il CF.';
      return;
    }
    
    try {
      const date = new Date(vals.dataNascita);
      const cf = new CodiceFiscale({
        name: vals.nome,
        surname: vals.cognome,
        gender: vals.sesso,
        day: date.getDate(),
        month: date.getMonth() + 1,
        year: date.getFullYear(),
        birthplace: vals.cittaNascita
      });
      this.registerForm.patchValue({ codiceFiscale: cf.code });
      this.errorMessage = '';
    } catch (e: any) {
      this.errorMessage = 'Errore nella generazione del CF: ' + e.message;
    }
  }
}
