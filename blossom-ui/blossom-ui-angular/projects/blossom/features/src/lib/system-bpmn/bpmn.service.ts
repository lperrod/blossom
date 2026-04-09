import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BpmnResponse {
  definitions: ProcessDefinition[];
}

export interface ProcessDefinition {
  id: string;
  key: string;
  name: string;
  version: number;
  deploymentId: string;
  resourceName: string;
  runningInstances: number;
}

@Injectable({ providedIn: 'root' })
export class BpmnService {
  private readonly API = '/blossom/api/system/bpmn';

  constructor(private http: HttpClient) {}

  get(): Observable<BpmnResponse> {
    return this.http.get<BpmnResponse>(this.API);
  }
}
