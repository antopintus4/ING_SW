import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ContentService } from '../../services/content.service';
import { LinkService } from '../../services/link.service';
import { GroupService } from '../../services/group.service';
import { MessageBoundaryComponent } from '../message-boundary/message-boundary.component';

declare var bootstrap: any;

@Component({
  selector: 'app-content-management-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, MessageBoundaryComponent],
  templateUrl: './content-management-boundary.component.html',
  styleUrl: './content-management-boundary.component.css'
})
export class ContentManagementBoundaryComponent implements OnInit {
  @ViewChild('messageBoundary') messageBoundary!: MessageBoundaryComponent;
  contents: any[] = [];
  errorMessage: string = '';
  successMessage: string = '';

  searchQuery: string = '';
  filterVisibility: string = '';
  filterType: string = '';
  currentPage: number = 0;
  pageSize: number = 15;
  hasMore: boolean = false;

  selectedContentIds: Set<string> = new Set();
  isCreatingNewGroup: boolean = false;
  newGroupName: string = '';

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
    private groupService: GroupService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadContents();
  }

  loadContents() {
    this.contentService.getContents(
      this.searchQuery,
      this.filterVisibility,
      this.filterType,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (data) => {
        this.contents = data;
        this.hasMore = data.length === this.pageSize;
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento dei contenuti.';
      }
    });
  }

  applyFilters() {
    this.currentPage = 0;
    this.loadContents();
  }

  resetFilters() {
    this.searchQuery = '';
    this.filterVisibility = '';
    this.filterType = '';
    this.currentPage = 0;
    this.loadContents();
  }

  nextPage() {
    if (this.hasMore) {
      this.currentPage++;
      this.loadContents();
    }
  }

  prevPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadContents();
    }
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
    this.router.navigate(['/content/preview'], { queryParams: { id: allegatoId } });
  }

  delete(id: string) {
    this.messageBoundary.showConfirmMessage(
      "Sei sicuro di voler eliminare questo contenuto?",
      () => {
        this.contentService.deleteContent(id).subscribe({
          next: () => {
            this.loadContents();
          },
          error: () => {
            this.errorMessage = 'Impossibile eliminare il contenuto.';
          }
        });
      }
    );
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
      const identificatore = this.generatedLink.split('/').pop();
      if (identificatore) {
        this.linkService.sendLinkViaEmail(this.shareEmail, identificatore).subscribe({
          next: () => {
            this.closeShareModal();
            this.messageBoundary.showMessage("Condivisione avvenuta con successo");
          },
          error: (err) => {
            if (err.status === 404) {
              this.messageBoundary.showMessage("Utente non trovato");
            } else {
              this.emailErrorMessage = err.error || 'Errore durante la condivisione.';
            }
          }
        });
      }
    }
  }

  openGroupModal(contentId?: string) {
    if (contentId) {
      this.selectedContentIds.clear();
      this.selectedContentIds.add(contentId);
    }

    if (this.selectedContentIds.size === 0) {
      this.errorMessage = 'Seleziona almeno un contenuto da raggruppare.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.isCreatingNewGroup = false;
    this.newGroupName = '';
    this.selectedGroupId = null;

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
    this.selectedGroupId = null;
    this.isCreatingNewGroup = false;
    this.newGroupName = '';
  }

  confirmGroupAggregation() {
    const contentIdsArray = Array.from(this.selectedContentIds);

    if (this.isCreatingNewGroup) {
      if (!this.newGroupName || this.newGroupName.trim() === '') {
        this.messageBoundary.showMessage("Uno o più campi obbligatori non rispettati");
        return;
      }

      this.groupService.createGroup(this.newGroupName.trim()).subscribe({
        next: (newGroup) => {
          this.groupService.addContentsToGroupMassive(newGroup.id, contentIdsArray).subscribe({
            next: () => {
              this.messageBoundary.showMessage("Contenuti aggiunti con successo");
              this.closeGroupModal();
              this.selectedContentIds.clear();
              this.loadContents();
            },
            error: () => {
              this.errorMessage = 'Errore durante l\'aggregazione dei contenuti al nuovo gruppo.';
              this.closeGroupModal();
            }
          });
        },
        error: () => {
          this.errorMessage = 'Errore durante la creazione del gruppo.';
          this.closeGroupModal();
        }
      });
    } else {
      if (!this.selectedGroupId) return;

      this.groupService.addContentsToGroupMassive(this.selectedGroupId, contentIdsArray).subscribe({
        next: () => {
          this.messageBoundary.showMessage("Contenuti aggiunti con successo");
          this.closeGroupModal();
          this.selectedContentIds.clear();
          this.loadContents();
        },
        error: (err) => {
          this.errorMessage = err.error || 'Errore durante l\'aggregazione al gruppo.';
          this.closeGroupModal();
        }
      });
    }
  }

  toggleSelection(id: string) {
    if (this.selectedContentIds.has(id)) {
      this.selectedContentIds.delete(id);
    } else {
      this.selectedContentIds.add(id);
    }
  }

  toggleAll(event: any) {
    if (event.target.checked) {
      this.contents.forEach(c => this.selectedContentIds.add(c.id));
    } else {
      this.selectedContentIds.clear();
    }
  }

  isSelected(id: string): boolean {
    return this.selectedContentIds.has(id);
  }

  isAllSelected(): boolean {
    return this.contents.length > 0 && this.selectedContentIds.size === this.contents.length;
  }
}
