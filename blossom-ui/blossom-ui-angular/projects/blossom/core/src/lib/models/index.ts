export interface UserInfo {
  id: string;
  identifier: string;
  firstname: string;
  lastname: string;
  email: string;
  locale: string;
  function?: string;
  company?: string;
  phone?: string;
}

export interface MenuItem {
  key: string;
  label: string;
  icon: string;
  link: string;
  level: number;
  order: number;
  privilege: string;
  leaf: boolean;
  items?: MenuItem[];
}

export interface AppConfiguration {
  user: UserInfo;
  authorities: string[];
  impersonating: boolean;
  menu: MenuItem[];
  locales: string[];
}

export interface PageMetadata {
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface Page<T> {
  content: T[];
  page: PageMetadata;
}

import { InjectionToken } from '@angular/core';

export interface BlossomAppConfig {
  appName: string;
}

export const BLOSSOM_APP_CONFIG = new InjectionToken<BlossomAppConfig>('BLOSSOM_APP_CONFIG', {
  providedIn: 'root',
  factory: () => ({ appName: 'Blossom' })
});
