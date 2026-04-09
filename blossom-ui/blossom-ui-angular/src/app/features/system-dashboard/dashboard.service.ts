import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly API = '/blossom/api/system/dashboard';

  constructor(private http: HttpClient) {}

  getStatus(): Observable<any> { return this.http.get<any>(`${this.API}/status`); }
  getMemory(): Observable<any> { return this.http.get<any>(`${this.API}/memory`); }
  getJvm(): Observable<any> { return this.http.get<any>(`${this.API}/jvm`); }
  getCharts(): Observable<any> { return this.http.get<any>(`${this.API}/charts`); }
}
