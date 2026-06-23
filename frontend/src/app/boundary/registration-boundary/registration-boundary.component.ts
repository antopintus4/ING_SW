import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import CodiceFiscale from 'codice-fiscale-js';

@Component({
  selector: 'app-registration-boundary',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './registration-boundary.component.html',
  styleUrl: './registration-boundary.component.css'
})
export class RegistrationBoundaryComponent implements OnInit {
  registerForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  comuni: any[] = [];
  comuniFiltrati: any[] = [];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private http: HttpClient
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

  ngOnInit() {
    this.http.get<any[]>('/comuni.json').subscribe({
      next: (data) => {
        this.comuni = data;
      },
      error: (err) => {
        console.error('Errore nel caricamento dei comuni:', err);
      }
    });

    this.registerForm.get('cittaNascita')?.valueChanges.subscribe(val => {
      this.filtraComuni(val);
    });
  }

  filtraComuni(val: string) {
    if (!val || val.length < 2) {
      this.comuniFiltrati = [];
      return;
    }
    const filterValue = val.toLowerCase();
    this.comuniFiltrati = this.comuni
      .filter(c => c.nome.toLowerCase().includes(filterValue))
      .slice(0, 10);
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

    const comuneScelto = this.comuni.find(c => c.nome.toLowerCase() === vals.cittaNascita.toLowerCase());
    if (!comuneScelto) {
      this.errorMessage = 'Città di nascita non riconosciuta nel database dei comuni italiani.';
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
        birthplace: comuneScelto.nome,
        birthplaceProvincia: comuneScelto.sigla
      });
      this.registerForm.patchValue({ codiceFiscale: (cf as any).code });
      this.errorMessage = '';
    } catch (e: any) {
      this.errorMessage = 'Errore nella generazione del CF: ' + e.message;
    }
  }
}
