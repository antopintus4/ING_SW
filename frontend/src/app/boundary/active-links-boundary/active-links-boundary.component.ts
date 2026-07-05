import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LinkService } from '../../services/link.service';
import { MessageBoundaryComponent } from '../message-boundary/message-boundary.component';

@Component({
  selector: 'app-active-links-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule, MessageBoundaryComponent],
  templateUrl: './active-links-boundary.component.html',
  styleUrl: './active-links-boundary.component.css'
})
export class ActiveLinksBoundaryComponent implements OnInit {
  @ViewChild('messageBoundary') messageBoundary!: MessageBoundaryComponent;
  links: any[] = [];
  errorMessage: string = '';

  constructor(private linkService: LinkService) {}

  ngOnInit() {
    this.loadLinks();
  }

  loadLinks() {
    this.linkService.getMyLinks().subscribe({
      next: (data) => {
        this.links = data;
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento dei link attivi.';
      }
    });
  }

  copyLink(identificatore: string) {
    const shareUrl = `${window.location.origin}/share/${identificatore}`;
    navigator.clipboard.writeText(shareUrl).then(() => {
      alert('Link copiato negli appunti!');
    });
  }

  disabilita(identificatore: string) {
    this.messageBoundary.showConfirmMessage(
      "Sei sicuro di voler disabilitare questo link?",
      () => {
        this.linkService.revokeLink(identificatore).subscribe({
          next: () => {
            this.messageBoundary.showMessage("Link disabilitato con successo");
            this.loadLinks();
          },
          error: () => {
            this.errorMessage = 'Errore durante la disabilitazione del link.';
          }
        });
      },
      undefined,
      'Conferma',
      'Annulla'
    );
  }
}
