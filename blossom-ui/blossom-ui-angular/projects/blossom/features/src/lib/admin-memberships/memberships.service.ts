import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MembershipDTO { id: string; a: any; b: any; }

@Injectable({ providedIn: 'root' })
export class MembershipsService {
  private readonly API = '/blossom/api/administration/memberships';
  constructor(private http: HttpClient) {}
  getByUser(userId: number): Observable<any[]> { return this.http.get<any[]>(this.API, { params: { userId: userId.toString() } }); }
  getByGroup(groupId: number): Observable<any[]> { return this.http.get<any[]>(this.API, { params: { groupId: groupId.toString() } }); }
  associate(userId: number, groupId: number): Observable<any> { return this.http.post<any>(this.API, { userId, groupId }); }
  dissociate(id: string): Observable<void> { return this.http.delete<void>(`${this.API}/${id}`); }
}
