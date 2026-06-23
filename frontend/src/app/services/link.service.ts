import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LinkService {
  private apiUrl = '/api/links';

  constructor(private http: HttpClient) {}

  createLink(contenutoId: number, daysToExpire: number): Observable<any> {
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

  downloadPublicFile(identificatore: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/shared/${identificatore}/download`, { responseType: 'blob' });
  }
}
