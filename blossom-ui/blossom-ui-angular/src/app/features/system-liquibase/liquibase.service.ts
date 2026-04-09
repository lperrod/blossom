import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LiquibaseService {
  private readonly API = '/blossom/api/system/liquibase';

  constructor(private http: HttpClient) {}

  getReports(): Observable<Record<string, any>> {
    return this.http.get<Record<string, any>>(this.API);
  }
}
