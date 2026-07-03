import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LinkService } from '../../services/link.service';

@Component({
  selector: 'app-active-links-boundary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './active-links-boundary.component.html',
  styleUrl: './active-links-boundary.component.css'
})
export class ActiveLinksBoundaryComponent implements OnInit {
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

  revoke(identificatore: string) {
    if (confirm('Sei sicuro di voler revocare questo link? Nessuno potrà più accedervi.')) {
      this.linkService.revokeLink(identificatore).subscribe({
        next: () => {
          this.loadLinks();
        },
        error: () => {
          this.errorMessage = 'Errore durante la revoca del link.';
        }
      });
    }
  }
}
