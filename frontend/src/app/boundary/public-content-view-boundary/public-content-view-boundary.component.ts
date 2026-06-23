import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
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

  constructor(
    private route: ActivatedRoute,
    private contentService: ContentService
  ) {}

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.loadContent(Number(idParam));
    }
  }

  loadContent(id: number) {
    this.contentService.getPublicContent(id).subscribe({
      next: (data) => this.contenuto = data,
      error: (err) => {
        if (err.status === 403) {
          this.errorMessage = "Non si dispone dell'autorizzazione necessaria per visualizzare questo contenuto.";
        } else {
          this.errorMessage = "Impossibile caricare il contenuto pubblico.";
        }
      }
    });
  }

  downloadAllegato(allegatoId: number, nomeFile: string) {
    this.contentService.downloadContent(allegatoId).subscribe({
      next: (blob: Blob) => {
        const a = document.createElement('a');
        const objectUrl = window.URL.createObjectURL(blob);
        a.href = objectUrl;
        a.download = nomeFile || 'download';
        a.click();
        window.URL.revokeObjectURL(objectUrl);
      },
      error: () => alert('Errore durante il download.')
    });
  }
}
