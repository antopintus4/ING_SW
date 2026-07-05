import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LinkService {
  private apiUrl = '/api/links';

  constructor(private http: HttpClient) {}

  createLink(contenutoId: string, daysToExpire: number): Observable<any> {
    return this.http.post(this.apiUrl, { contenutoId, daysToExpire });
  }

  getMyLinks(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  revokeLink(identificatore: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${identificatore}`, { responseType: 'text' });
  }

  getPublicLinkDetails(identificatore: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/shared/${identificatore}`);
  }

  downloadPublicFile(identificatore: string, allegatoId?: string): Observable<Blob> {
    const url = allegatoId 
      ? `${this.apiUrl}/shared/${identificatore}/download?allegatoId=${allegatoId}`
      : `${this.apiUrl}/shared/${identificatore}/download`;
    return this.http.get(url, { responseType: 'blob' });
  }

  sendLinkViaEmail(email: string, identificatore: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/send`, { email, identificatore });
  }

  getLinkDetails(identificatore: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${identificatore}`);
  }

  updateLinkSettings(identificatore: string, dataScadenza: string, risorseIncluse: string[]): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${identificatore}`, { dataScadenza, risorseIncluse });
  }
}
