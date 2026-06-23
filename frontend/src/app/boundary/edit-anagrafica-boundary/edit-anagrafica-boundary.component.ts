import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ProfileService } from '../../services/profile.service';

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

  constructor(private profileService: ProfileService, private router: Router) {}

  ngOnInit() {
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
    this.profileService.updateAnagrafica(this.anagrafica).subscribe({
      next: () => {
        this.successMessage = 'Dati anagrafici aggiornati con successo!';
        setTimeout(() => this.router.navigate(['/profile']), 2000);
      },
      error: () => this.errorMessage = 'Errore durante il salvataggio dei dati.'
    });
  }
}
