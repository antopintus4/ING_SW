import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LinkService } from '../../services/link.service';
import { MessageBoundaryComponent } from '../message-boundary/message-boundary.component';

@Component({
  selector: 'app-link-settings-boundary',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MessageBoundaryComponent],
  templateUrl: './link-settings-boundary.component.html',
  styleUrl: './link-settings-boundary.component.css'
})
export class LinkSettingsBoundaryComponent implements OnInit {
  @ViewChild('messageBoundary') messageBoundary!: MessageBoundaryComponent;
  
  linkId: string = '';
  linkDetails: any = null;
  dataScadenza: string = '';
  risorse: any[] = [];
  errorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private linkService: LinkService
  ) {}

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('identificatore');
    if (idParam) {
      this.linkId = idParam;
      this.loadLinkDetails();
    } else {
      this.errorMessage = 'Link non identificato.';
    }
  }

  loadLinkDetails() {
    this.linkService.getLinkDetails(this.linkId).subscribe({
      next: (data) => {
        this.linkDetails = data;
        
        // Formatta dataScadenza per l'input datetime-local (YYYY-MM-DDTHH:MM)
        if (data.dataScadenza) {
          this.dataScadenza = data.dataScadenza.substring(0, 16);
        }

        // Mappa gli allegati associati al contenuto
        if (data.contenuto && data.contenuto.allegati) {
          const settings = data.impostazioni;
          this.risorse = data.contenuto.allegati.map((a: any) => {
            let isIncluded = true;
            // Se le impostazioni contengono risorseIncluse, controlla la presenza dell'ID
            if (settings && settings.includes('risorseIncluse')) {
              isIncluded = settings.includes(a.id);
            }
            return {
              id: a.id,
              urlFile: a.urlFile,
              checked: isIncluded
            };
          });
        }
      },
      error: () => {
        this.errorMessage = 'Impossibile caricare i dettagli del link.';
      }
    });
  }

  salva() {
    this.errorMessage = '';
    const selectedIds = this.risorse.filter(r => r.checked).map(r => r.id);

    this.linkService.updateLinkSettings(this.linkId, this.dataScadenza, selectedIds).subscribe({
      next: () => {
        this.messageBoundary.showMessage("Impostazioni aggiornate con successo", () => {
          this.router.navigate(['/links']);
        });
      },
      error: () => {
        this.errorMessage = 'Errore durante il salvataggio delle impostazioni.';
      }
    });
  }

  indietro() {
    this.router.navigate(['/links']);
  }
}
