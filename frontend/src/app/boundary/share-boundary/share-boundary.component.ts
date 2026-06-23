import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
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

  constructor(
    private route: ActivatedRoute,
    private linkService: LinkService
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

  download() {
    this.linkService.downloadPublicFile(this.identificatore).subscribe({
      next: (blob) => {
        const a = document.createElement('a');
        const objectUrl = URL.createObjectURL(blob);
        a.href = objectUrl;
        a.download = this.contenuto.titolo || 'download_file';
        a.click();
        URL.revokeObjectURL(objectUrl);
      },
      error: () => {
        alert('Errore durante lo scaricamento del file.');
      }
    });
  }
}
