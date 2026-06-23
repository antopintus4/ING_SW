import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ContentService } from '../../services/content.service';

@Component({
  selector: 'app-content-view-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './content-view-boundary.component.html',
  styleUrl: './content-view-boundary.component.css'
})
export class ContentViewBoundaryComponent implements OnInit {
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
    this.contentService.getContent(id).subscribe({
      next: (data) => {
        this.contenuto = data;
      },
      error: () => {
        this.errorMessage = 'Contenuto non trovato o accesso negato.';
      }
    });
  }

  download() {
    if (this.contenuto && this.contenuto.allegati && this.contenuto.allegati.length > 0) {
      const allegatoId = this.contenuto.allegati[0].id;
      this.contentService.downloadContent(allegatoId).subscribe((blob) => {
        const a = document.createElement('a');
        const objectUrl = URL.createObjectURL(blob);
        a.href = objectUrl;
        a.download = this.contenuto.titolo;
        a.click();
        URL.revokeObjectURL(objectUrl);
      });
    }
  }
}
