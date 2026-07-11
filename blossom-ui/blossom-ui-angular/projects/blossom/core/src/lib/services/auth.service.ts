import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { toObservable } from '@angular/core/rxjs-interop';
import { UserInfo } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_URL = '/blossom/api/auth';
  private http = inject(HttpClient);

  private _currentUser = signal<UserInfo | null>(null);
  readonly currentUser = this._currentUser.asReadonly();
  currentUser$ = toObservable(this._currentUser);

  login(username: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/login`, { username, password }).pipe(
      tap(user => this._currentUser.set(user))
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/logout`, {}).pipe(
      tap(() => this._currentUser.set(null))
    );
  }

  getCurrentUser(): Observable<UserInfo> {
    return this.http.get<UserInfo>(`${this.API_URL}/current-user`).pipe(
      tap(user => this._currentUser.set(user))
    );
  }

  setUser(user: UserInfo): void {
    this._currentUser.set(user);
  }

  get isAuthenticated(): boolean {
    return this._currentUser() !== null;
  }
}
