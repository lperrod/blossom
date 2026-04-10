import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '@blossom/core';

export interface ArticleDTO { id: string; name: string; summary: string; content: string; status: string; creationDate: string; modificationDate: string; }

@Injectable({ providedIn: 'root' })
export class ArticlesService {
  private readonly API = '/blossom/api/content/articles';
  constructor(private http: HttpClient) {}
  list(q = '', page = 0, size = 25): Observable<Page<ArticleDTO>> { return this.http.get<Page<ArticleDTO>>(this.API, { params: { page: page.toString(), size: size.toString(), ...(q ? { q } : {}) } }); }
  get(id: string): Observable<ArticleDTO> { return this.http.get<ArticleDTO>(`${this.API}/${id}`); }
  create(form: any): Observable<ArticleDTO> { return this.http.post<ArticleDTO>(this.API, form); }
  update(id: string, form: any): Observable<ArticleDTO> { return this.http.put<ArticleDTO>(`${this.API}/${id}`, form); }
  delete(id: string, force = false): Observable<any> { return this.http.delete(`${this.API}/${id}`, { params: { force: force.toString() } }); }
}
