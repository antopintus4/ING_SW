import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api';
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient, @Inject(PLATFORM_ID) private platformId: Object) {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('token');
      this.isAuthenticatedSubject.next(!!token);
    }
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/registration/register`, userData, { responseType: 'text' });
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/login`, credentials).pipe(
      tap((res: any) => {
        if (res.token && isPlatformBrowser(this.platformId)) {
          localStorage.setItem('token', res.token);
          this.isAuthenticatedSubject.next(true);
        }
      })
    );
  }

  verifyOtp(username: string, otp: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/verify-otp`, { username, otp }).pipe(
      tap((res: any) => {
        if (res.token && isPlatformBrowser(this.platformId)) {
          localStorage.setItem('token', res.token);
          this.isAuthenticatedSubject.next(true);
        }
      })
    );
  }

  guestLogin(): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/guest-login`, {}).pipe(
      tap((res: any) => {
        if (res.token && isPlatformBrowser(this.platformId)) {
          localStorage.setItem('token', res.token);
          this.isAuthenticatedSubject.next(true);
        }
      })
    );
  }

  getRole(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role || null;
    } catch (e) {
      return null;
    }
  }

  setup2fa(): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/2fa/setup`, null, { responseType: 'text' });
  }

  confirm2fa(otp: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/2fa/confirm`, { otp }, { responseType: 'text' });
  }

  logout(): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/logout`, null, { responseType: 'text' }).pipe(
      tap(() => {
        if (isPlatformBrowser(this.platformId)) {
          localStorage.removeItem('token');
          this.isAuthenticatedSubject.next(false);
        }
      })
    );
  }

  clearSession() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
      this.isAuthenticatedSubject.next(false);
    }
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('token');
    }
    return null;
  }

  updateToken(newToken: string) {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('token', newToken);
    }
  }

  verifyRegistrationOtp(username: string, otp: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/registration/verify-otp`, { username, otp });
  }
}
