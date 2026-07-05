import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const AuthGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.getToken() && authService.getRole() === 'AFAM') {
    return true;
  } else {
    // If not logged in, or if logged in but as GUEST, redirect to login
    router.navigate(['/login']);
    return false;
  }
};
