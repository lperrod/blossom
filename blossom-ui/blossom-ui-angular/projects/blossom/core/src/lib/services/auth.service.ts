import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { UserInfo } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_URL = '/blossom/api/auth';
  private currentUserSubject = new BehaviorSubject<UserInfo | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/login`, { username, password }).pipe(
      tap(user => this.currentUserSubject.next(user))
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/logout`, {}).pipe(
      tap(() => this.currentUserSubject.next(null))
    );
  }

  getCurrentUser(): Observable<UserInfo> {
    return this.http.get<UserInfo>(`${this.API_URL}/current-user`).pipe(
      tap(user => this.currentUserSubject.next(user))
    );
  }

  setUser(user: UserInfo): void {
    this.currentUserSubject.next(user);
  }

  get isAuthenticated(): boolean {
    return this.currentUserSubject.value !== null;
  }
}
