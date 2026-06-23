import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ContentService {
  private apiUrl = '/api/contenuti';

  constructor(private http: HttpClient) {}

  uploadContent(file: File, titolo: string, descrizione: string, policyVisibilita: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('titolo', titolo);
    formData.append('descrizione', descrizione);
    formData.append('policyVisibilita', policyVisibilita);

    return this.http.post(`${this.apiUrl}/upload`, formData, { responseType: 'text' });
  }

  getContents(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  getContent(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  downloadContent(allegatoId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download/${allegatoId}/file`, { responseType: 'blob' });
  }

  deleteContent(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
  }

  getPublicContent(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/public/${id}`);
  }
}
