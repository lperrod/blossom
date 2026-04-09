import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '@blossom/core';

export interface FileDTO { id: number; name: string; path: string; contentType: string; extension: string; size: number; tags: string[]; hash: string; }

@Injectable({ providedIn: 'root' })
export class FileManagerService {
  private readonly API = '/blossom/api/content/filemanager';
  constructor(private http: HttpClient) {}
  list(q = '', page = 0, size = 25): Observable<Page<FileDTO>> { return this.http.get<Page<FileDTO>>(this.API, { params: { page: page.toString(), size: size.toString(), ...(q ? { q } : {}) } }); }
  get(id: number): Observable<FileDTO> { return this.http.get<FileDTO>(`${this.API}/${id}`); }
  upload(file: File): Observable<FileDTO> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<FileDTO>(this.API, formData);
  }
}
