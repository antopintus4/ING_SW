import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GroupService {
  private apiUrl = '/api/gruppi';

  constructor(private http: HttpClient) {}

  getGroups(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  getGroup(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  createGroup(nome: string): Observable<any> {
    return this.http.post<any>(this.apiUrl, { nome });
  }

  deleteGroup(id: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  addContentToGroup(groupId: string, contentId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${groupId}/contenuti/${contentId}`, {});
  }

  removeContentFromGroup(groupId: string, contentId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${groupId}/contenuti/${contentId}`);
  }

  updateGroup(id: string, nome: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, { nome });
  }
}
