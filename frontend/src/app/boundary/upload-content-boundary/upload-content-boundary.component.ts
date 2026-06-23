import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ContentService } from '../../services/content.service';

@Component({
  selector: 'app-upload-content-boundary',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './upload-content-boundary.component.html',
  styleUrl: './upload-content-boundary.component.css'
})
export class UploadContentBoundaryComponent {
  uploadForm: FormGroup;
  selectedFile: File | null = null;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(private fb: FormBuilder, private contentService: ContentService) {
    this.uploadForm = this.fb.group({
      titolo: ['', Validators.required],
      descrizione: ['', Validators.required],
      policyVisibilita: ['Pubblico', Validators.required]
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      if (file.size > 10 * 1024 * 1024) { // 10MB limit check client side
        this.errorMessage = 'Il file supera i 10MB consentiti.';
        this.selectedFile = null;
      } else {
        this.selectedFile = file;
        this.errorMessage = '';
      }
    }
  }

  onSubmit() {
    if (this.uploadForm.invalid || !this.selectedFile) {
      this.errorMessage = 'Compila tutti i campi e seleziona un file.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    const { titolo, descrizione, policyVisibilita } = this.uploadForm.value;

    this.contentService.uploadContent(this.selectedFile, titolo, descrizione, policyVisibilita).subscribe({
      next: (res) => {
        this.successMessage = 'Contenuto caricato con successo!';
        this.uploadForm.reset({ policyVisibilita: 'Pubblico' });
        this.selectedFile = null;
      },
      error: (err) => {
        this.errorMessage = 'Errore durante l\'upload: ' + (err.error || err.message);
      }
    });
  }
}
