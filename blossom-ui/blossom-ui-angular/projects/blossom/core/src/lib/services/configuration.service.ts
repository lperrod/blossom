import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AppConfiguration } from '../models';

@Injectable({ providedIn: 'root' })
export class ConfigurationService {
  private readonly API_URL = '/blossom/api/configuration';
  private configSubject = new BehaviorSubject<AppConfiguration | null>(null);
  config$ = this.configSubject.asObservable();

  constructor(private http: HttpClient) {}

  load(): Observable<AppConfiguration> {
    return this.http.get<AppConfiguration>(this.API_URL).pipe(
      tap(config => this.configSubject.next(config))
    );
  }

  get config(): AppConfiguration | null {
    return this.configSubject.value;
  }
}
