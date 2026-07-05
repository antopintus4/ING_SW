import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import CodiceFiscale from 'codice-fiscale-js';
import { AppComponent } from '../../app.component';

@Component({
  selector: 'app-registration-boundary',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './registration-boundary.component.html',
  styleUrl: './registration-boundary.component.css'
})
export class RegistrationBoundaryComponent implements OnInit {
  registerForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  comuni: any[] = [];
  comuniFiltrati: any[] = [];
  currentStep: number = 1;
  otpCode: string = '';
  registeredUsername: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private appComponent: AppComponent
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      email: ['', [Validators.required, Validators.pattern('^[a-zA-Z0-9_!#$%&\\\'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$')]],
      password: ['', [Validators.required, Validators.minLength(12)]],
      nome: ['', Validators.required],
      cognome: ['', Validators.required],
      sesso: ['', Validators.required],
      dataNascita: ['', Validators.required],
      cittaNascita: ['', Validators.required],
      codiceFiscale: ['', [Validators.required, Validators.pattern('^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$')]],
      istituzione: ['', Validators.required],
      dominioIstituzionale: ['', Validators.required],
      matricola: ['', Validators.required],
      corsoDiStudi: ['', Validators.required],
      annoAccademico: ['', Validators.required]
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

  nextStep() {
    this.errorMessage = '';
    if (this.currentStep === 1) {
      if (this.registerForm.get('username')?.invalid || this.registerForm.get('email')?.invalid || this.registerForm.get('password')?.invalid) {
        this.errorMessage = 'Compila correttamente i campi del primo step.';
        return;
      }
      this.currentStep++;
    } else if (this.currentStep === 2) {
      if (this.registerForm.get('nome')?.invalid || this.registerForm.get('cognome')?.invalid || this.registerForm.get('sesso')?.invalid ||
          this.registerForm.get('dataNascita')?.invalid || this.registerForm.get('cittaNascita')?.invalid || this.registerForm.get('codiceFiscale')?.invalid) {
        this.errorMessage = 'Compila correttamente i campi anagrafici e genera il codice fiscale.';
        return;
      }
      this.currentStep++;
    }
  }

  previousStep() {
    this.errorMessage = '';
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  onSubmit() {
    if (this.currentStep !== 3) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    const username = this.registerForm.get('username')?.value;

    this.authService.register(this.registerForm.value).subscribe({
      next: (res) => {
        this.registeredUsername = username;
        this.appComponent.messageBoundary.sendMessage(
          "Registrazione profilo avvenuta con successo. Controllare la mail per confermare l'account.",
          () => {
            this.currentStep = 4;
          }
        );
      },
      error: (err) => {
        this.errorMessage = err.error || 'Errore durante la registrazione. Riprova.';
      }
    });
  }

  onVerifyOtp() {
    if (!this.otpCode || !this.otpCode.match('^[0-9]{6}$')) {
      this.errorMessage = 'Inserisci un codice OTP valido di 6 cifre.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.authService.verifyRegistrationOtp(this.registeredUsername, this.otpCode).subscribe({
      next: (res: any) => {
        const msg = res.message || 'Registrazione completata e verificata con successo! Ora puoi effettuare il login.';
        this.appComponent.messageBoundary.sendMessage(
          msg,
          () => {
            this.registerForm.reset();
            this.otpCode = '';
            this.router.navigate(['/login']);
          }
        );
      },
      error: (err) => {
        this.appComponent.messageBoundary.sendMessage(
          err.error || 'Codice OTP errato o scaduto. Riprova.'
        );
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
