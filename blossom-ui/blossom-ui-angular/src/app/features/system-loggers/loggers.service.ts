import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoggersService {
  private readonly API = '/blossom/api/system/loggers';

  constructor(private http: HttpClient) {}

  list(): Observable<any> {
    return this.http.get<any>(this.API);
  }

  get(name: string): Observable<any> {
    return this.http.get<any>(`${this.API}/${name}`);
  }

  setLevel(name: string, level: string): Observable<void> {
    return this.http.post<void>(`${this.API}/${name}/${level}`, {});
  }

  tree(q = ''): Observable<any> {
    return this.http.get<any>(`${this.API}/tree`, { params: q ? { q } : {} });
  }
}
