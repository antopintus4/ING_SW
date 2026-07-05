import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ContentService } from '../../services/content.service';

@Component({
  selector: 'app-public-content-view-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './public-content-view-boundary.component.html',
  styleUrl: './public-content-view-boundary.component.css'
})
export class PublicContentViewBoundaryComponent implements OnInit {
  contenuto: any = null;
  errorMessage: string = '';
  mediaUrl: SafeUrl | null = null;
  mediaType: string = '';

  constructor(
    private route: ActivatedRoute,
    private contentService: ContentService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.loadContent(idParam);
    }
  }

  loadContent(id: string) {
    this.contentService.getPublicContent(id).subscribe({
      next: (data) => {
        this.contenuto = data;
        if (this.contenuto && this.contenuto.allegati && this.contenuto.allegati.length > 0) {
          const allegatoId = this.contenuto.allegati[0].id;
          this.contentService.downloadContent(allegatoId).subscribe((blob) => {
            const file = new Blob([blob], { type: blob.type });
            const objectUrl = window.URL.createObjectURL(file);
            this.mediaUrl = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
            this.mediaType = blob.type;

            const fileName = this.contenuto.allegati[0].urlFile.toLowerCase();
            if (!this.mediaType || this.mediaType === 'application/octet-stream') {
              if (fileName.endsWith('.pdf')) this.mediaType = 'application/pdf';
              else if (fileName.match(/\.(png|jpg|jpeg|gif)$/)) this.mediaType = 'image/jpeg';
              else if (fileName.match(/\.(mp4|webm)$/)) this.mediaType = 'video/mp4';
              else if (fileName.match(/\.(mp3|wav)$/)) this.mediaType = 'audio/mpeg';
            }
          });
        }
      },
      error: (err) => {
        if (err.status === 403) {
          this.errorMessage = "Non si dispone dell'autorizzazione necessaria per visualizzare questo contenuto.";
        } else {
          this.errorMessage = "Impossibile caricare il contenuto pubblico.";
        }
      }
    });
  }

  download(allegatoId: string, filename: string = 'file') {
    this.contentService.downloadContent(allegatoId).subscribe({
      next: (blob: Blob) => {
        const a = document.createElement('a');
        const objectUrl = window.URL.createObjectURL(blob);
        a.href = objectUrl;
        a.download = filename || 'download';
        a.click();
        window.URL.revokeObjectURL(objectUrl);
      },
      error: () => alert('Errore durante il download.')
    });
  }

  view(allegatoId: string) {
    this.contentService.downloadContent(allegatoId).subscribe({
      next: (blob: Blob) => {
        const file = new Blob([blob], { type: blob.type });
        const objectUrl = window.URL.createObjectURL(file);
        window.open(objectUrl, '_blank');
      },
      error: () => alert('Errore durante la visualizzazione.')
    });
  }
}
