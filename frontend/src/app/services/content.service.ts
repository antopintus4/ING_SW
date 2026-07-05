import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ContentService {
  private apiUrl = '/api/contenuti';

  constructor(private http: HttpClient) {}

  uploadContent(files: File[], descrizioneFile: File | null, titolo: string, policyVisibilita: string, autori?: string, collaboratori?: string): Observable<any> {
    const formData = new FormData();
    files.forEach(file => {
      formData.append('file', file);
    });
    if (descrizioneFile) {
      formData.append('descrizioneFile', descrizioneFile);
    }
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

  getContents(q?: string, policyVisibilita?: string, tipo?: string, page: number = 0, size: number = 15): Observable<any[]> {
    let params: any = {
      page: page.toString(),
      size: size.toString()
    };
    if (q) params.q = q;
    if (policyVisibilita) params.policyVisibilita = policyVisibilita;
    if (tipo) params.tipo = tipo;

    return this.http.get<any[]>(this.apiUrl, { params });
  }

  getContent(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  downloadContent(allegatoId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download/${allegatoId}/file`, { responseType: 'blob' });
  }

  deleteContent(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
  }

  getPublicContent(id: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/public/${id}`);
  }

  updateContent(id: string, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, data);
  }

  addAllegato(contentId: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/${contentId}/allegati`, formData);
  }

  deleteAllegato(contentId: string, allegatoId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${contentId}/allegati/${allegatoId}`, { responseType: 'text' });
  }
}
