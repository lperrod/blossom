import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CacheInfo {
  name: string;
  enabled: boolean;
  size: number;
  hits: number;
  misses: number;
  evictions: number;
}

@Injectable({ providedIn: 'root' })
export class CachesService {
  private readonly API = '/blossom/api/system/caches';

  constructor(private http: HttpClient) {}

  list(q = ''): Observable<CacheInfo[]> {
    return this.http.get<CacheInfo[]>(this.API, { params: q ? { q } : {} });
  }

  emptyCache(name: string): Observable<void> {
    return this.http.post<void>(`${this.API}/${name}/_empty`, {});
  }

  disableCache(name: string): Observable<void> {
    return this.http.post<void>(`${this.API}/${name}/_disable`, {});
  }

  enableCache(name: string): Observable<void> {
    return this.http.post<void>(`${this.API}/${name}/_enable`, {});
  }

  emptyAll(): Observable<void> {
    return this.http.post<void>(`${this.API}/_empty`, {});
  }
}
