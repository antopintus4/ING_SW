import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 1. Ignora gli errori generati dalle richieste di ping fatte dal componente di riconnessione
      if (req.url.includes('/api/auth/ping')) {
        return throwError(() => error);
      }

      // 2. Se l'utente si trova già sulla pagina di errore, evita ulteriori redirect ridondanti
      if (router.url.includes('/error-msg') || router.url.includes('/error')) {
        return throwError(() => error);
      }

      // Errore di Rete (server irraggiungibile)
      if (error.status === 0) {
        router.navigate(['/error-msg'], { queryParams: { type: 'network', redirectUrl: router.url } });
      } 
      // Errore Interno Server
      else if (error.status === 500 || error.status === 503) {
        router.navigate(['/error-msg'], { queryParams: { type: 'server' } });
      }
      // Non Autorizzato: pulisci sessione ed invia al login se appropriato
      else if (error.status === 401) {
        // Ignora i 401 del login (es. credenziali errate)
        if (!req.url.includes('/api/auth/login')) {
          authService.clearSession(); // Pulisce cache locale per evitare loop
          router.navigate(['/login']);
        }
      }

      // Re-throw per i controller locali che vogliono gestirlo
      return throwError(() => error);
    })
  );
};
