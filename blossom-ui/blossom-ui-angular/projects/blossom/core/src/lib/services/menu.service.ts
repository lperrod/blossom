import { Injectable, computed, inject } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { MenuItem } from '../models';
import { ConfigurationService } from './configuration.service';

@Injectable({ providedIn: 'root' })
export class MenuService {
  private configService = inject(ConfigurationService);

  readonly menu = computed<MenuItem[]>(() => {
    const config = this.configService.config();
    return config?.menu ?? [];
  });

  menu$ = toObservable(this.menu);
}
