import { Component, OnInit, ViewChild } from '@angular/core';
import { RouterOutlet, RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';
import { MessageBoundaryComponent } from './boundary/message-boundary/message-boundary.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterModule, CommonModule, MessageBoundaryComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  @ViewChild('messageBoundary') messageBoundary!: MessageBoundaryComponent;
  title = 'frontend';
  isAuthenticated = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.authService.isAuthenticated$.subscribe(
      (isAuth) => {
        this.isAuthenticated = isAuth;
      }
    );
  }

  get isAuthor(): boolean {
    return this.isAuthenticated && this.authService.getRole() === 'AFAM';
  }

  logout() {
    this.messageBoundary.showConfirmMessage(
      "Sei sicuro di voler confermare l'uscita dall'account?",
      () => {
        this.authService.logout().subscribe({
          next: () => {
            this.router.navigate(['/login']);
          },
          error: () => {
            this.authService.clearSession();
            this.router.navigate(['/login']);
          }
        });
      }
    );
  }

  onSearch(event: any) {
    event.preventDefault();
    const query = event.target.elements.q.value;
    if (query) {
      const trimmedQuery = query.trim();
      const prefixMatch = trimmedQuery.match(/^(user|group|author|collab|collaborator):(.*)$/i);
      
      if (prefixMatch) {
        const val = prefixMatch[2].trim();
        if (val.length >= 2) {
          this.router.navigate(['/search'], { queryParams: { q: trimmedQuery } });
          event.target.elements.q.value = '';
        }
      } else if (trimmedQuery.length >= 2) {
        this.router.navigate(['/search'], { queryParams: { q: trimmedQuery } });
        event.target.elements.q.value = '';
      }
    }
  }
}
