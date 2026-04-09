import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '@blossom/core';

export interface GroupDTO { id: number; name: string; description: string; }

@Injectable({ providedIn: 'root' })
export class GroupsService {
  private readonly API = '/blossom/api/administration/groups';
  constructor(private http: HttpClient) {}
  list(q = '', page = 0, size = 25): Observable<Page<GroupDTO>> { return this.http.get<Page<GroupDTO>>(this.API, { params: { page: page.toString(), size: size.toString(), ...(q ? { q } : {}) } }); }
  get(id: number): Observable<GroupDTO> { return this.http.get<GroupDTO>(`${this.API}/${id}`); }
  create(form: any): Observable<GroupDTO> { return this.http.post<GroupDTO>(this.API, form); }
  update(id: number, form: any): Observable<GroupDTO> { return this.http.put<GroupDTO>(`${this.API}/${id}`, form); }
  delete(id: number, force = false): Observable<any> { return this.http.delete(`${this.API}/${id}`, { params: { force: force.toString() } }); }
}
