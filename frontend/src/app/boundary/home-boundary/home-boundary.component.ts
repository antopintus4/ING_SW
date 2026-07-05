import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home-boundary.component.html',
  styleUrl: './home-boundary.component.css'
})
export class HomeBoundaryComponent implements OnInit {
  isAuthenticated: boolean = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.authService.isAuthenticated$.subscribe(isAuth => {
      this.isAuthenticated = isAuth;
    });
  }

  get isAuthor(): boolean {
    return this.isAuthenticated && this.authService.getRole() === 'AFAM';
  }

  guestLogin() {
    this.authService.guestLogin().subscribe({
      next: () => {
        this.router.navigate(['/']);
      }
    });
  }
}
