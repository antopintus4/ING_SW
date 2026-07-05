import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-other-edits-boundary',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './other-edits-boundary.component.html',
  styleUrl: './other-edits-boundary.component.css'
})
export class OtherEditsBoundaryComponent implements OnInit {
  otherEdits: any = {
    policyVisibilita: 'Privato',
    descrizione: '',
    interessi: [],
    competenze: []
  };

  // Sezione attualmente attiva: 'privacy' | 'descrizione' | 'hobby' | 'skills' | null
  activeSection: 'privacy' | 'descrizione' | 'hobby' | 'skills' | null = null;

  // Campi specifici per la form privacy
  isPublic: boolean = false;
  isContactsVisible: boolean = false;

  // Campi temporanei come stringhe separate da virgola per input facilitato
  interessiStr: string = '';
  competenzeStr: string = '';

  errorMessage: string = '';
  successMessage: string = '';
  showSuccessModal: boolean = false;

  constructor(private profileService: ProfileService, private router: Router) { }

  ngOnInit() {
    this.profileService.getMe().subscribe({
      next: (data) => {
        if (data) {
          this.otherEdits.policyVisibilita = data.policyVisibilita || 'Privato';
          this.otherEdits.descrizione = data.descrizione || '';
          this.otherEdits.interessi = data.interessi || [];
          this.otherEdits.competenze = data.competenze || [];

          // Inizializza i checkbox basandoti sul valore corrente di policyVisibilita strutturata
          this.isPublic = this.otherEdits.policyVisibilita.startsWith('Pubblico');
          this.isContactsVisible = this.otherEdits.policyVisibilita.endsWith('_Contatti');

          this.interessiStr = this.otherEdits.interessi.join(', ');
          this.competenzeStr = this.otherEdits.competenze.join(', ');
        }
      },
      error: () => this.errorMessage = 'Errore nel caricamento dei dati.'
    });
  }

  onPublicToggle() {
    if (!this.isPublic) {
      this.isContactsVisible = false;
    }
  }

  selectSection(section: 'privacy' | 'descrizione' | 'hobby' | 'skills') {
    this.activeSection = section;
    this.errorMessage = '';
    this.successMessage = '';
    this.showSuccessModal = false;
  }

  confermaModifica() {
    this.errorMessage = '';
    this.successMessage = '';

    // Prepariamo la copia aggiornata dei dati del profilo
    const updatedPayload = { ...this.otherEdits };

    if (this.activeSection === 'privacy') {
      // Mappatura strutturata basata sullo stato delle due checkbox
      if (this.isPublic) {
        updatedPayload.policyVisibilita = this.isContactsVisible ? 'Pubblico_Contatti' : 'Pubblico';
      } else {
        updatedPayload.policyVisibilita = 'Privato';
      }
    }
    else if (this.activeSection === 'descrizione') {
      // Inserisce nel campo "Descrizione" una nuova descrizione
      // Già legato a updatedPayload.descrizione tramite ngModel
    }
    else if (this.activeSection === 'hobby') {
      // Modifica il contenuto del campo "Hobby" (interessi)
      updatedPayload.interessi = this.interessiStr
        .split(',')
        .map(s => s.trim())
        .filter(s => s.length > 0);
    }
    else if (this.activeSection === 'skills') {
      // Modifica le proprie skill (competenze)
      updatedPayload.competenze = this.competenzeStr
        .split(',')
        .map(s => s.trim())
        .filter(s => s.length > 0);
    }

    // Invia al backend
    this.profileService.updateOtherEdits(updatedPayload).subscribe({
      next: (res) => {
        // Aggiorna lo stato locale con la risposta
        this.otherEdits.policyVisibilita = res.policyVisibilita || 'Privato';
        this.otherEdits.descrizione = res.descrizione || '';
        this.otherEdits.interessi = res.interessi || [];
        this.otherEdits.competenze = res.competenze || [];

        // Aggiorna le stringhe locali
        this.interessiStr = this.otherEdits.interessi.join(', ');
        this.competenzeStr = this.otherEdits.competenze.join(', ');
        this.isPublic = this.otherEdits.policyVisibilita.startsWith('Pubblico');
        this.isContactsVisible = this.otherEdits.policyVisibilita.endsWith('_Contatti');

        // Mostra il messaggio "Modifica avvenuta con successo"
        this.successMessage = 'Modifica avvenuta con successo';
        this.showSuccessModal = true;
      },
      error: (err) => {
        this.errorMessage = 'Errore durante il salvataggio dei dati. Si prega di verificare la connessione.';
      }
    });
  }

  confermaOk() {
    this.showSuccessModal = false;
    // Ritorna a Il Mio Profilo
    this.router.navigate(['/profile']);
  }
}
