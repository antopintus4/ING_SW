import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ContentService } from '../../services/content.service';

@Component({
  selector: 'app-preview-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './preview-boundary.component.html',
  styleUrl: './preview-boundary.component.css'
})
export class PreviewBoundaryComponent implements OnInit, OnDestroy {
  allegatoId: string = '';
  fileUrl: string = '';
  safeFileUrl: SafeResourceUrl | null = null;
  mimeType: string = '';
  isImage: boolean = false;
  isAudio: boolean = false;
  isVideo: boolean = false;
  isPdf: boolean = false;
  isLoading: boolean = true;
  errorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private contentService: ContentService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.allegatoId = params['id'] || '';
      if (this.allegatoId) {
        this.loadPreview();
      } else {
        this.isLoading = false;
        this.errorMessage = 'ID allegato non specificato.';
      }
    });
  }

  loadPreview() {
    this.isLoading = true;
    this.errorMessage = '';
    this.contentService.downloadContent(this.allegatoId).subscribe({
      next: (blob) => {
        this.mimeType = blob.type;
        this.fileUrl = URL.createObjectURL(blob);
        
        if (this.mimeType.startsWith('image/')) {
          this.isImage = true;
        } else if (this.mimeType.startsWith('audio/')) {
          this.isAudio = true;
        } else if (this.mimeType.startsWith('video/')) {
          this.isVideo = true;
        } else if (this.mimeType === 'application/pdf') {
          this.isPdf = true;
          this.safeFileUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.fileUrl);
        }
        
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Errore nel caricamento dell\'anteprima del file.';
      }
    });
  }

  ngOnDestroy() {
    if (this.fileUrl) {
      URL.revokeObjectURL(this.fileUrl);
    }
  }
}
