import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private readonly API = '/blossom/api/search';

  constructor(private http: HttpClient) {}

  search(q: string, page = 0, size = 25): Observable<any> {
    return this.http.get<any>(this.API, { params: { q, page: page.toString(), size: size.toString() } });
  }
}
