import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ProfileService } from '../../services/profile.service';
import { HttpClient } from '@angular/common/http';
import CodiceFiscale from 'codice-fiscale-js';

@Component({
  selector: 'app-edit-anagrafica-boundary',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './edit-anagrafica-boundary.component.html',
  styleUrl: './edit-anagrafica-boundary.component.css'
})
export class EditAnagraficaBoundaryComponent implements OnInit {
  anagrafica: any = {
    nome: '',
    cognome: '',
    dataNascita: '',
    codiceFiscale: '',
    citta: '',
    indirizzo: '',
    telefono: ''
  };
  errorMessage: string = '';
  successMessage: string = '';
  sesso: string = ''; // Transient field just for CF calculation
  comuni: any[] = [];

  constructor(private profileService: ProfileService, private router: Router, private http: HttpClient) {}

  ngOnInit() {
    this.http.get<any[]>('/comuni.json').subscribe({
      next: (data) => this.comuni = data,
      error: (err) => console.error('Errore nel caricamento dei comuni:', err)
    });
    this.profileService.getMe().subscribe({
      next: (data) => {
        if (data) {
          this.anagrafica.nome = data.nome || '';
          this.anagrafica.cognome = data.cognome || '';
          this.anagrafica.dataNascita = data.dataNascita || '';
          this.anagrafica.codiceFiscale = data.codiceFiscale || '';
          this.anagrafica.citta = data.citta || '';
          this.anagrafica.indirizzo = data.indirizzo || '';
          this.anagrafica.telefono = data.telefono || '';
        }
      },
      error: () => this.errorMessage = 'Errore nel caricamento dei dati.'
    });
  }

  save() {
    this.anagrafica.sesso = this.sesso;
    this.profileService.updateAnagrafica(this.anagrafica).subscribe({
      next: () => {
        this.successMessage = 'Dati anagrafici aggiornati con successo!';
        setTimeout(() => this.router.navigate(['/profile']), 2000);
      },
      error: () => this.errorMessage = 'Errore durante il salvataggio dei dati.'
    });
  }

  generaCF() {
    const vals = this.anagrafica;
    if (!vals.nome || !vals.cognome || !this.sesso || !vals.dataNascita || !vals.citta) {
      this.errorMessage = 'Compila nome, cognome, sesso, data e città di nascita per ricalcolare il CF.';
      return;
    }

    const comuneScelto = this.comuni.find(c => c.nome.toLowerCase() === vals.citta.toLowerCase());
    if (!comuneScelto) {
      this.errorMessage = 'Città di nascita non riconosciuta nel database dei comuni italiani.';
      return;
    }
    
    try {
      const date = new Date(vals.dataNascita);
      const cf = new CodiceFiscale({
        name: vals.nome,
        surname: vals.cognome,
        gender: this.sesso as 'M' | 'F',
        day: date.getDate(),
        month: date.getMonth() + 1,
        year: date.getFullYear(),
        birthplace: comuneScelto.nome,
        birthplaceProvincia: comuneScelto.sigla
      });
      this.anagrafica.codiceFiscale = (cf as any).code;
      this.errorMessage = '';
    } catch (e: any) {
      this.errorMessage = 'Errore nella generazione del CF: ' + e.message;
    }
  }
}
