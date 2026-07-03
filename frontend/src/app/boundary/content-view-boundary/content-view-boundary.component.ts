import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { ContentService } from '../../services/content.service';
import { LinkService } from '../../services/link.service';
import { GroupService } from '../../services/group.service';

declare var bootstrap: any;

@Component({
  selector: 'app-content-view-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './content-view-boundary.component.html',
  styleUrl: './content-view-boundary.component.css'
})
export class ContentViewBoundaryComponent implements OnInit {
  contenuto: any = null;
  errorMessage: string = '';
  successMessage: string = '';
  mediaUrl: SafeUrl | null = null;
  mediaType: string = '';

  groups: any[] = [];
  selectedGroupId: string | null = null;
  groupModalInstance: any;

  shareModalInstance: any;
  scadenzaGiorni: number = 7;
  generatedLink: string = '';
  emailDestinatario: string = '';
  emailSuccessMessage: string = '';
  emailErrorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private contentService: ContentService,
    private linkService: LinkService,
    private groupService: GroupService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.loadContent(idParam);
    }
  }

  loadContent(id: string) {
    this.contentService.getContent(id).subscribe({
      next: (data) => {
        this.contenuto = data;
        if (this.contenuto && this.contenuto.allegati && this.contenuto.allegati.length > 0) {
          const allegatoId = this.contenuto.allegati[0].id;
          this.contentService.downloadContent(allegatoId).subscribe((blob) => {
            const file = new Blob([blob], { type: blob.type });
            const objectUrl = URL.createObjectURL(file);
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

  view() {
    if (this.contenuto && this.contenuto.allegati && this.contenuto.allegati.length > 0) {
      const allegatoId = this.contenuto.allegati[0].id;
      this.contentService.downloadContent(allegatoId).subscribe((blob) => {
        // Create a blob with the actual mime type determined by the backend
        const file = new Blob([blob], { type: blob.type });
        const objectUrl = URL.createObjectURL(file);
        window.open(objectUrl, '_blank');
      });
    }
  }

  delete() {
    if (this.contenuto && confirm('Sei sicuro di voler eliminare questo contenuto?')) {
      this.contentService.deleteContent(this.contenuto.id).subscribe({
        next: () => {
          this.router.navigate(['/content']);
        },
        error: () => this.errorMessage = 'Errore durante l\'eliminazione.'
      });
    }
  }

  share() {
    this.scadenzaGiorni = 7;
    this.generatedLink = '';
    this.emailDestinatario = '';
    this.emailSuccessMessage = '';
    this.emailErrorMessage = '';
    const modalEl = document.getElementById('shareModal');
    if (modalEl) {
      this.shareModalInstance = new bootstrap.Modal(modalEl);
      this.shareModalInstance.show();
    }
  }

  closeShareModal() {
    if (this.shareModalInstance) {
      this.shareModalInstance.hide();
    }
  }

  generateLink() {
    if (this.contenuto) {
      this.linkService.createLink(this.contenuto.id, this.scadenzaGiorni).subscribe({
        next: (res) => {
          const origin = window.location.origin;
          this.generatedLink = `${origin}/public/content/${res.identificatoreLink}`;
        },
        error: () => this.errorMessage = 'Errore durante la generazione del link.'
      });
    }
  }

  copyLink() {
    if (this.generatedLink) {
      navigator.clipboard.writeText(this.generatedLink).then(() => {
        alert('Link copiato negli appunti!');
      });
    }
  }

  sendEmail() {
    if (!this.emailDestinatario || !this.emailDestinatario.includes('@')) {
      this.emailErrorMessage = 'Inserisci un indirizzo email valido.';
      return;
    }
    this.emailErrorMessage = '';
    this.emailSuccessMessage = '';
    
    if (this.contenuto) {
      this.linkService.sendLinkViaEmail(this.contenuto.id, this.emailDestinatario).subscribe({
        next: () => {
          this.emailSuccessMessage = 'E-mail inviata con successo!';
          this.emailDestinatario = '';
        },
        error: () => {
          this.emailErrorMessage = 'Errore durante l\'invio dell\'e-mail.';
        }
      });
    }
  }

  openGroupModal() {
    this.groupService.getGroups().subscribe({
      next: (data) => {
        this.groups = data;
        const modalEl = document.getElementById('groupModal');
        if (modalEl) {
          this.groupModalInstance = new bootstrap.Modal(modalEl);
          this.groupModalInstance.show();
        }
      },
      error: () => this.errorMessage = 'Errore nel caricamento dei gruppi.'
    });
  }

  closeGroupModal() {
    if (this.groupModalInstance) {
      this.groupModalInstance.hide();
    }
    this.selectedGroupId = null;
  }

  confirmGroupAggregation() {
    if (this.contenuto && this.selectedGroupId) {
      this.groupService.addContentToGroup(this.selectedGroupId, this.contenuto.id).subscribe({
        next: () => {
          this.successMessage = 'Contenuto aggregato al gruppo con successo!';
          this.closeGroupModal();
        },
        error: () => this.errorMessage = 'Errore durante l\'aggregazione al gruppo.'
      });
    }
  }
}
