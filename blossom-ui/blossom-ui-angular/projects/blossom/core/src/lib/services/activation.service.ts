import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ActivationService {
  private readonly API = '/blossom/api/public/activation';

  constructor(private http: HttpClient) {}

  requestPasswordReset(loginOrEmail: string): Observable<void> {
    return this.http.post<void>(`${this.API}/forgotten_password`, { loginOrEmail });
  }

  validateToken(token: string): Observable<{ valid: boolean; userId?: number }> {
    return this.http.post<{ valid: boolean; userId?: number }>(`${this.API}/validate_token`, { token });
  }

  changePassword(token: string, password: string, passwordRepeater: string): Observable<void> {
    return this.http.post<void>(`${this.API}/change_password`, { token, password, passwordRepeater });
  }

  activate(token: string): Observable<{ resetToken: string }> {
    return this.http.get<{ resetToken: string }>(`${this.API}/activate`, { params: { token } });
  }
}
