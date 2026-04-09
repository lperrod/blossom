import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { MenuItem } from '../models';
import { ConfigurationService } from './configuration.service';

@Injectable({ providedIn: 'root' })
export class MenuService {
  private menuSubject = new BehaviorSubject<MenuItem[]>([]);
  menu$ = this.menuSubject.asObservable();

  constructor(private configService: ConfigurationService) {
    this.configService.config$.subscribe(config => {
      if (config) {
        this.menuSubject.next(config.menu);
      }
    });
  }

  get menu(): MenuItem[] {
    return this.menuSubject.value;
  }
}
