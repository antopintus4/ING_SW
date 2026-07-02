import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private apiUrl = '/api/profile';

  constructor(private http: HttpClient) {}

  getMe(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/me`);
  }

  updateAnagrafica(data: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/anagrafica`, data);
  }

  updateCredentials(payload: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/credentials`, payload);
  }

  getPublicProfile(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/public/${id}`);
  }

  updatePassword(data: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/password`, data);
  }

  updateOtherEdits(data: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/other`, data);
  }
}
