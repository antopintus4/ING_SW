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

  selectedContentId: number | null = null;
  scadenzaGiorni: number = 7;
  shareModalInstance: any;

  groups: any[] = [];
  selectedGroupId: number | null = null;
  groupModalInstance: any;

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

  download(allegatoId: number, filename: string = 'file') {
    this.contentService.downloadContent(allegatoId).subscribe((blob) => {
      const a = document.createElement('a');
      const objectUrl = URL.createObjectURL(blob);
      a.href = objectUrl;
      a.download = filename;
      a.click();
      URL.revokeObjectURL(objectUrl);
    });
  }

  delete(id: number) {
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

  share(contenutoId: number) {
    const days = prompt('Inserisci il numero di giorni di validità del link (es. 7):', '7');
    if (days != null && !isNaN(Number(days))) {
      this.linkService.createLink(contenutoId, Number(days)).subscribe({
        next: (link) => {
          this.successMessage = `Link generato: ${window.location.origin}/share/${link.identificatoreLink}`;
        },
        error: () => {
          this.errorMessage = 'Errore durante la generazione del link.';
        }
      });
    }
  }

  openGroupModal(contentId: number) {
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
    if (this.selectedContentId && this.selectedGroupId) {
      this.groupService.addContentToGroup(this.selectedGroupId, this.selectedContentId).subscribe({
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
