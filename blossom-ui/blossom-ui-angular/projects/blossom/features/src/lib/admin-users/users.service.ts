import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '@blossom/core';

export interface UserDTO {
  id: string; identifier: string; firstname: string; lastname: string; email: string;
  phone: string; company: string; function: string; description: string; civility: string;
  activated: boolean; locale: string; lastConnection: string;
}

@Injectable({ providedIn: 'root' })
export class UsersService {
  private readonly API = '/blossom/api/administration/users';

  constructor(private http: HttpClient) {}

  list(q = '', page = 0, size = 25): Observable<Page<UserDTO>> {
    const params: any = { page: page.toString(), size: size.toString() };
    if (q) params.q = q;
    return this.http.get<Page<UserDTO>>(this.API, { params });
  }
  get(id: string): Observable<UserDTO> { return this.http.get<UserDTO>(`${this.API}/${id}`); }
  create(form: any): Observable<UserDTO> { return this.http.post<UserDTO>(this.API, form); }
  update(id: string, form: any): Observable<UserDTO> { return this.http.put<UserDTO>(`${this.API}/${id}`, form); }
  delete(id: string, force = false): Observable<any> { return this.http.delete(`${this.API}/${id}`, { params: { force: force.toString() } }); }
}
