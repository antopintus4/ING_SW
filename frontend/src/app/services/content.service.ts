import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ContentService {
  private apiUrl = '/api/contenuti';

  constructor(private http: HttpClient) {}

  uploadContent(file: File, descrizioneFile: File, titolo: string, policyVisibilita: string, autori?: string, collaboratori?: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('descrizioneFile', descrizioneFile);
    formData.append('titolo', titolo);
    formData.append('policyVisibilita', policyVisibilita);
    if (autori) {
      formData.append('autori', autori);
    }
    if (collaboratori) {
      formData.append('collaboratori', collaboratori);
    }

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
