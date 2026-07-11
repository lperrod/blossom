import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { toObservable } from '@angular/core/rxjs-interop';
import { AppConfiguration } from '../models';

@Injectable({ providedIn: 'root' })
export class ConfigurationService {
  private readonly API_URL = '/blossom/api/configuration';
  private http = inject(HttpClient);

  private _config = signal<AppConfiguration | null>(null);
  readonly config = this._config.asReadonly();
  config$ = toObservable(this._config);

  load(): Observable<AppConfiguration> {
    return this.http.get<AppConfiguration>(this.API_URL).pipe(
      tap(config => this._config.set(config))
    );
  }
}
