import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ResponsibilitiesService {
  private readonly API = '/blossom/api/administration/responsabilities';
  constructor(private http: HttpClient) {}
  getByUser(userId: number): Observable<any[]> { return this.http.get<any[]>(this.API, { params: { userId: userId.toString() } }); }
  getByRole(roleId: number): Observable<any[]> { return this.http.get<any[]>(this.API, { params: { roleId: roleId.toString() } }); }
  associate(userId: number, roleId: number): Observable<any> { return this.http.post<any>(this.API, { userId, roleId }); }
  dissociate(id: number): Observable<void> { return this.http.delete<void>(`${this.API}/${id}`); }
}
