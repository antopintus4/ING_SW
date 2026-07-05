import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-public-profile-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './public-profile-boundary.component.html',
  styleUrl: './public-profile-boundary.component.css'
})
export class PublicProfileBoundaryComponent implements OnInit {
  profilo: any = null;
  errorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private profileService: ProfileService
  ) {}

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.loadProfile(idParam);
    }
  }

  loadProfile(id: string) {
    this.profileService.getPublicProfile(id).subscribe({
      next: (data) => this.profilo = data,
      error: () => this.errorMessage = 'Profilo non trovato o non disponibile.'
    });
  }
}
