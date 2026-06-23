import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RecoveryService {
  private apiUrl = 'http://localhost:8080/api/recovery';

  constructor(private http: HttpClient) {}

  requestPasswordRecovery(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/request`, { email }, { responseType: 'text' });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset`, { token, newPassword }, { responseType: 'text' });
  }

  requestEmailRecovery(username: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/request-email`, { username });
  }
}
