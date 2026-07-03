import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SearchService {
  private apiUrl = '/api/search';

  constructor(private http: HttpClient) {}

  searchGlobal(query: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}?q=${encodeURIComponent(query)}`);
  }
}
