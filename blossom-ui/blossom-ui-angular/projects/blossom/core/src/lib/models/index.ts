export interface UserInfo {
  id: number;
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

export interface Page<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface SearchResult<T> {
  page: Page<T>;
}
