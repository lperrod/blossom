import { Injectable, computed, inject } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { MenuItem } from '../models';
import { ConfigurationService } from './configuration.service';

@Injectable({ providedIn: 'root' })
export class MenuService {
  private configService = inject(ConfigurationService);

  readonly menu = computed<MenuItem[]>(() => {
    const config = this.configService.config();
    if (!config) {
      return [];
    }
    return this.filterByAuthorities(config.menu, config.authorities);
  });

  menu$ = toObservable(this.menu);

  private filterByAuthorities(items: MenuItem[], authorities: string[]): MenuItem[] {
    return items
      .map(item => {
        const filteredChildren = item.items
          ? this.filterByAuthorities(item.items, authorities)
          : undefined;
        return { ...item, items: filteredChildren };
      })
      .filter(item => {
        // Keep if no privilege required or user has the privilege
        const hasPrivilege = !item.privilege || authorities.includes(item.privilege);
        // For parent items (with children), also require at least one visible child
        const hasVisibleChildren = !item.items || item.items.length > 0;
        return hasPrivilege && hasVisibleChildren;
      });
  }
}
