import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly API = '/blossom/api/profile';

  constructor(private http: HttpClient) {}

  get(): Observable<any> { return this.http.get<any>(this.API); }

  updatePassword(password: string, passwordRepeater: string): Observable<any> {
    return this.http.put<any>(`${this.API}/password`, { password, passwordRepeater });
  }
}
