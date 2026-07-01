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
    policyVisibilita: 'Pubblico',
    descrizione: '',
    interessi: [],
    competenze: []
  };

  interessiStr: string = '';
  competenzeStr: string = '';

  errorMessage: string = '';
  successMessage: string = '';

  constructor(private profileService: ProfileService, private router: Router) {}

  ngOnInit() {
    this.profileService.getMe().subscribe({
      next: (data) => {
        if (data) {
          this.otherEdits.policyVisibilita = data.policyVisibilita || 'Pubblico';
          this.otherEdits.descrizione = data.descrizione || '';
          this.otherEdits.interessi = data.interessi || [];
          this.otherEdits.competenze = data.competenze || [];

          this.interessiStr = this.otherEdits.interessi.join(', ');
          this.competenzeStr = this.otherEdits.competenze.join(', ');
        }
      },
      error: () => this.errorMessage = 'Errore nel caricamento dei dati.'
    });
  }

  save() {
    // Convert comma-separated strings back to arrays
    this.otherEdits.interessi = this.interessiStr.split(',').map(s => s.trim()).filter(s => s);
    this.otherEdits.competenze = this.competenzeStr.split(',').map(s => s.trim()).filter(s => s);

    this.profileService.updateOtherEdits(this.otherEdits).subscribe({
      next: () => {
        this.successMessage = 'Altre modifiche salvate con successo!';
        setTimeout(() => this.router.navigate(['/profile']), 2000);
      },
      error: () => this.errorMessage = 'Errore durante il salvataggio dei dati.'
    });
  }
}
