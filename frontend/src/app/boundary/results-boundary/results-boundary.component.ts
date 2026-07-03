import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { SearchService } from '../../services/search.service';

@Component({
  selector: 'app-results-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './results-boundary.component.html',
  styleUrl: './results-boundary.component.css'
})
export class ResultsBoundaryComponent implements OnInit {
  query: string = '';
  utenti: any[] = [];
  contenuti: any[] = [];
  gruppi: any[] = [];
  errorMessage: string = '';
  isSearching: boolean = false;

  constructor(private route: ActivatedRoute, private searchService: SearchService) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.query = params['q'] || '';
      if (this.query.length >= 2) {
        this.performSearch();
      }
    });
  }

  performSearch() {
    this.isSearching = true;
    this.errorMessage = '';
    
    this.searchService.searchGlobal(this.query).subscribe({
      next: (res) => {
        this.utenti = res.utenti || [];
        this.contenuti = res.contenuti || [];
        this.gruppi = res.gruppi || [];
        this.isSearching = false;
      },
      error: (err) => {
        this.isSearching = false;
        this.errorMessage = 'Errore durante la ricerca.';
      }
    });
  }
}
