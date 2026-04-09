import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SessionsResponse {
  sessions: SessionInfo[];
  loginAttempts: Record<string, Record<string, number>>;
}

export interface SessionInfo {
  sessionId: string;
  principal: string;
  lastRequest: string;
  expired: boolean;
}

@Injectable({ providedIn: 'root' })
export class SessionsService {
  private readonly API = '/blossom/api/system/sessions';

  constructor(private http: HttpClient) {}

  list(): Observable<SessionsResponse> {
    return this.http.get<SessionsResponse>(this.API);
  }

  invalidate(sessionId: string): Observable<void> {
    return this.http.post<void>(`${this.API}/${sessionId}/_invalidate`, {});
  }
}
