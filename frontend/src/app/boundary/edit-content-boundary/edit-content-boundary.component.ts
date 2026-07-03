import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ContentService } from '../../services/content.service';

@Component({
  selector: 'app-edit-content-boundary',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './edit-content-boundary.component.html',
  styleUrl: './edit-content-boundary.component.css'
})
export class EditContentBoundaryComponent implements OnInit {
  editForm: FormGroup;
  contentId: string | null = null;
  content: any = null;
  errorMessage: string = '';
  successMessage: string = '';
  selectedFile: File | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private contentService: ContentService
  ) {
    this.editForm = this.fb.group({
      titolo: ['', Validators.required],
      policyVisibilita: ['Pubblico', Validators.required],
      autori: [''],
      collaboratori: [''],
      descrizione: ['']
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.contentId = idParam;
      this.loadContent();
    }
  }

  loadContent() {
    if (this.contentId) {
      this.contentService.getContent(this.contentId).subscribe({
        next: (data) => {
          this.content = data;
          this.editForm.patchValue({
            titolo: data.titolo,
            policyVisibilita: data.policyVisibilita,
            autori: data.autori ? data.autori.join(',') : '',
            collaboratori: data.collaboratori ? data.collaboratori.join(',') : '',
            descrizione: '' // Di default la descrizione è vuota, verrà appesa
          });
        },
        error: () => {
          this.errorMessage = 'Errore nel caricamento del contenuto.';
        }
      });
    }
  }

  onSubmit() {
    if (this.editForm.invalid || !this.contentId) return;

    this.errorMessage = '';
    this.successMessage = '';

    this.contentService.updateContent(this.contentId, this.editForm.value).subscribe({
      next: (res) => {
        this.successMessage = 'Metadati aggiornati con successo!';
        this.loadContent(); // Ricarica per visualizzare le modifiche
      },
      error: (err) => {
        this.errorMessage = 'Errore durante l\'aggiornamento.';
      }
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  uploadAllegato() {
    if (!this.selectedFile || !this.contentId) {
      this.errorMessage = 'Seleziona un file da allegare.';
      return;
    }
    
    this.contentService.addAllegato(this.contentId, this.selectedFile).subscribe({
      next: () => {
        this.successMessage = 'Allegato aggiunto con successo!';
        this.selectedFile = null;
        this.loadContent();
      },
      error: () => {
        this.errorMessage = 'Errore durante l\'aggiunta dell\'allegato.';
      }
    });
  }

  deleteAllegato(allegatoId: string) {
    if (!this.contentId) return;
    if (confirm('Sei sicuro di voler eliminare questo allegato?')) {
      this.contentService.deleteAllegato(this.contentId, allegatoId).subscribe({
        next: () => {
          this.successMessage = 'Allegato eliminato.';
          this.loadContent();
        },
        error: () => {
          this.errorMessage = 'Errore durante l\'eliminazione dell\'allegato.';
        }
      });
    }
  }
}
