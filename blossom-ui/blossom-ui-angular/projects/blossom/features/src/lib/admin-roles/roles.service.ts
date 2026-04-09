import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '@blossom/core';

export interface RoleDTO { id: number; name: string; description: string; privileges: string[]; }

@Injectable({ providedIn: 'root' })
export class RolesService {
  private readonly API = '/blossom/api/administration/roles';
  private readonly PRIV_API = '/blossom/api/administration/privileges';
  constructor(private http: HttpClient) {}
  list(q = '', page = 0, size = 25): Observable<Page<RoleDTO>> { return this.http.get<Page<RoleDTO>>(this.API, { params: { page: page.toString(), size: size.toString(), ...(q ? { q } : {}) } }); }
  get(id: number): Observable<RoleDTO> { return this.http.get<RoleDTO>(`${this.API}/${id}`); }
  create(form: any): Observable<RoleDTO> { return this.http.post<RoleDTO>(this.API, form); }
  update(id: number, form: any): Observable<RoleDTO> { return this.http.put<RoleDTO>(`${this.API}/${id}`, form); }
  delete(id: number, force = false): Observable<any> { return this.http.delete(`${this.API}/${id}`, { params: { force: force.toString() } }); }
  getPrivileges(): Observable<any[]> { return this.http.get<any[]>(this.PRIV_API); }
}
