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
  selectedFiles: File[] = [];
  selectedDescrizioneFile: File | null = null;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(private fb: FormBuilder, private contentService: ContentService) {
    this.uploadForm = this.fb.group({
      titolo: ['', Validators.required],
      policyVisibilita: ['Pubblico', Validators.required],
      autori: [''],
      collaboratori: ['']
    });
  }

  onFileSelected(event: any) {
    const files: FileList = event.target.files;
    this.selectedFiles = [];
    this.errorMessage = '';

    if (files) {
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (file.size > 10 * 1024 * 1024) { // 10MB limit check client side
          this.errorMessage = `Il file ${file.name} supera i 10MB consentiti.`;
          this.selectedFiles = [];
          return;
        }
        this.selectedFiles.push(file);
      }
    }
  }

  onDescrizioneFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      if (file.type !== 'text/plain') {
        this.errorMessage = 'Il file di descrizione deve essere un file di testo (.txt).';
        this.selectedDescrizioneFile = null;
      } else {
        this.selectedDescrizioneFile = file;
        this.errorMessage = '';
      }
    }
  }

  onSubmit() {
    if (this.uploadForm.invalid || this.selectedFiles.length === 0) {
      this.errorMessage = 'Compila tutti i campi obbligatori e seleziona almeno un file principale.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    const { titolo, policyVisibilita, autori, collaboratori } = this.uploadForm.value;

    this.contentService.uploadContent(
      this.selectedFiles,
      this.selectedDescrizioneFile,
      titolo,
      policyVisibilita,
      autori,
      collaboratori
    ).subscribe({
      next: (res) => {
        this.successMessage = 'Contenuto caricato con successo!';
        this.uploadForm.reset({ policyVisibilita: 'Pubblico' });
        this.selectedFiles = [];
        this.selectedDescrizioneFile = null;
      },
      error: (err) => {
        this.errorMessage = 'Errore durante l\'upload: ' + (err.error || err.message);
      }
    });
  }
}
