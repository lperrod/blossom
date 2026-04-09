import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SchedulerResponse { info: any; groups: string[]; }
export interface JobInfo { name: string; group: string; description: string; jobClass: string; nextFireTime: string; previousFireTime: string; state: string; cronExpression?: string; }

@Injectable({ providedIn: 'root' })
export class SchedulerService {
  private readonly API = '/blossom/api/system/scheduler';

  constructor(private http: HttpClient) {}

  getInfo(): Observable<SchedulerResponse> { return this.http.get<SchedulerResponse>(this.API); }
  getJobs(group: string): Observable<JobInfo[]> { return this.http.get<JobInfo[]>(`${this.API}/${group}`); }
  getJob(group: string, name: string): Observable<JobInfo> { return this.http.get<JobInfo>(`${this.API}/${group}/${name}`); }
  execute(group: string, name: string): Observable<void> { return this.http.post<void>(`${this.API}/${group}/${name}/_execute`, {}); }
  changeState(state: boolean): Observable<void> { return this.http.post<void>(`${this.API}/_changeState`, {}, { params: { state: state.toString() } }); }
}
