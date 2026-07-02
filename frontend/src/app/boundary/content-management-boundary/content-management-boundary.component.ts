import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ContentService } from '../../services/content.service';
import { LinkService } from '../../services/link.service';
import { GroupService } from '../../services/group.service';

declare var bootstrap: any;

@Component({
  selector: 'app-content-management-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './content-management-boundary.component.html',
  styleUrl: './content-management-boundary.component.css'
})
export class ContentManagementBoundaryComponent implements OnInit {
  contents: any[] = [];
  errorMessage: string = '';
  successMessage: string = '';

  selectedContentId: string | null = null;
  scadenzaGiorni: number = 7;

  groups: any[] = [];
  selectedGroupId: string | null = null;
  groupModalInstance: any;

  shareModalInstance: any;
  shareEmail: string = '';
  generatedLink: string = '';
  emailSuccessMessage: string = '';
  emailErrorMessage: string = '';

  constructor(
    private contentService: ContentService, 
    private linkService: LinkService,
    private groupService: GroupService
  ) {}

  ngOnInit() {
    this.loadContents();
  }

  loadContents() {
    this.contentService.getContents().subscribe({
      next: (data) => {
        this.contents = data;
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento dei contenuti.';
      }
    });
  }

  download(allegatoId: string, filename: string = 'file') {
    this.contentService.downloadContent(allegatoId).subscribe((blob) => {
      const a = document.createElement('a');
      const objectUrl = URL.createObjectURL(blob);
      a.href = objectUrl;
      a.download = filename;
      a.click();
      URL.revokeObjectURL(objectUrl);
    });
  }

  view(allegatoId: string) {
    this.contentService.downloadContent(allegatoId).subscribe((blob) => {
      const file = new Blob([blob], { type: blob.type });
      const objectUrl = URL.createObjectURL(file);
      window.open(objectUrl, '_blank');
    });
  }

  delete(id: string) {
    if (confirm('Sei sicuro di voler eliminare questo contenuto?')) {
      this.contentService.deleteContent(id).subscribe({
        next: () => {
          this.loadContents();
        },
        error: () => {
          this.errorMessage = 'Impossibile eliminare il contenuto.';
        }
      });
    }
  }

  share(contenutoId: string) {
    this.selectedContentId = contenutoId;
    this.scadenzaGiorni = 7;
    this.generatedLink = '';
    this.shareEmail = '';
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
    this.selectedContentId = null;
  }

  generateLink() {
    if (this.selectedContentId && this.scadenzaGiorni > 0) {
      this.linkService.createLink(this.selectedContentId, this.scadenzaGiorni).subscribe({
        next: (link) => {
          this.generatedLink = `${window.location.origin}/share/${link.identificatoreLink}`;
        },
        error: () => {
          this.errorMessage = 'Errore durante la generazione del link.';
          this.closeShareModal();
        }
      });
    }
  }

  copyLink() {
    if (this.generatedLink) {
      navigator.clipboard.writeText(this.generatedLink).then(() => {
        // Optional: show a small toast or temporary indicator
      });
    }
  }

  sendEmail() {
    if (this.shareEmail && this.generatedLink) {
      this.emailSuccessMessage = '';
      this.emailErrorMessage = '';
      // Assumes the linkService has a sendEmail method that accepts the email and the link identifier
      // We extract the identifier from the generated link
      const identificatore = this.generatedLink.split('/').pop();
      if (identificatore) {
        this.linkService.sendLinkViaEmail(this.shareEmail, identificatore).subscribe({
          next: () => {
            this.emailSuccessMessage = 'E-mail inviata con successo!';
          },
          error: () => {
            this.emailErrorMessage = 'Errore durante l\'invio dell\'e-mail.';
          }
        });
      }
    }
  }

  openGroupModal(contentId: string) {
    this.selectedContentId = contentId;
    this.groupService.getGroups().subscribe({
      next: (data) => {
        this.groups = data;
        const modalEl = document.getElementById('groupModal');
        if (modalEl) {
          this.groupModalInstance = new bootstrap.Modal(modalEl);
          this.groupModalInstance.show();
        }
      },
      error: () => this.errorMessage = 'Impossibile caricare i gruppi.'
    });
  }

  closeGroupModal() {
    if (this.groupModalInstance) {
      this.groupModalInstance.hide();
    }
    this.selectedContentId = null;
    this.selectedGroupId = null;
  }

  confirmGroupAggregation() {
    if (this.selectedGroupId && this.selectedContentId) {
      this.groupService.addContentToGroup(this.selectedGroupId.toString(), this.selectedContentId.toString()).subscribe({
        next: () => {
          this.successMessage = 'Contenuto aggregato al gruppo con successo!';
          this.closeGroupModal();
          setTimeout(() => this.successMessage = '', 4000);
        },
        error: (err) => {
          this.errorMessage = err.error || 'Errore durante l\'aggregazione al gruppo.';
          this.closeGroupModal();
        }
      });
    }
  }
}
