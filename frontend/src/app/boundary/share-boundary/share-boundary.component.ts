import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { LinkService } from '../../services/link.service';

@Component({
  selector: 'app-share-boundary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './share-boundary.component.html',
  styleUrl: './share-boundary.component.css'
})
export class ShareBoundaryComponent implements OnInit {
  contenuto: any = null;
  errorMessage: string = '';
  identificatore: string = '';
  
  selectedAllegatoIndex: number = 0;
  mediaUrl: SafeResourceUrl | null = null;
  mediaType: string = '';

  constructor(
    private route: ActivatedRoute,
    private linkService: LinkService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('identificatore');
    if (idParam) {
      this.identificatore = idParam;
      this.loadSharedContent();
    } else {
      this.errorMessage = 'Link non valido.';
    }
  }

  loadSharedContent() {
    this.linkService.getPublicLinkDetails(this.identificatore).subscribe({
      next: (data) => {
        this.contenuto = data;
        if (data && data.allegati && data.allegati.length > 0) {
          this.selectAllegato(0);
        }
      },
      error: (err) => {
        if (err.status === 410) {
          this.errorMessage = 'Questo link è scaduto o è stato revocato dal proprietario.';
        } else {
          this.errorMessage = 'Impossibile caricare il contenuto condiviso.';
        }
      }
    });
  }

  selectAllegato(index: number) {
    this.selectedAllegatoIndex = index;
    this.mediaUrl = null;
    this.mediaType = '';

    if (this.contenuto && this.contenuto.allegati && this.contenuto.allegati.length > index) {
      const allegato = this.contenuto.allegati[index];
      this.linkService.downloadPublicFile(this.identificatore, allegato.id).subscribe({
        next: (blob) => {
          const file = new Blob([blob], { type: blob.type });
          const objectUrl = URL.createObjectURL(file);
          this.mediaUrl = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
          this.mediaType = blob.type;

          const fileName = allegato.urlFile.toLowerCase();
          if (!this.mediaType || this.mediaType === 'application/octet-stream') {
            if (fileName.endsWith('.pdf')) this.mediaType = 'application/pdf';
            else if (fileName.match(/\.(png|jpg|jpeg|gif)$/)) this.mediaType = 'image/jpeg';
            else if (fileName.match(/\.(mp4|webm)$/)) this.mediaType = 'video/mp4';
            else if (fileName.match(/\.(mp3|wav)$/)) this.mediaType = 'audio/mpeg';
          }
        }
      });
    }
  }

  download() {
    if (this.contenuto && this.contenuto.allegati && this.contenuto.allegati.length > this.selectedAllegatoIndex) {
      const allegato = this.contenuto.allegati[this.selectedAllegatoIndex];
      this.linkService.downloadPublicFile(this.identificatore, allegato.id).subscribe({
        next: (blob) => {
          const a = document.createElement('a');
          const objectUrl = URL.createObjectURL(blob);
          a.href = objectUrl;
          a.download = allegato.urlFile;
          a.click();
          URL.revokeObjectURL(objectUrl);
        },
        error: () => {
          alert('Errore durante lo scaricamento del file.');
        }
      });
    }
  }

  view() {
    if (this.contenuto && this.contenuto.allegati && this.contenuto.allegati.length > this.selectedAllegatoIndex) {
      const allegato = this.contenuto.allegati[this.selectedAllegatoIndex];
      this.linkService.downloadPublicFile(this.identificatore, allegato.id).subscribe({
        next: (blob) => {
          const file = new Blob([blob], { type: blob.type });
          const objectUrl = URL.createObjectURL(file);
          window.open(objectUrl, '_blank');
        },
        error: () => {
          alert('Errore durante la visualizzazione del file.');
        }
      });
    }
  }
}
