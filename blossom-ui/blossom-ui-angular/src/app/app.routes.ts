import { Routes } from '@angular/router';
import { LayoutComponent, LoginComponent, ErrorPageComponent } from '@blossom/shell';
import { authGuard, privilegeGuard } from '@blossom/core';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      // Home
      {
        path: '',
        loadChildren: () => import('./features/home/routes').then(m => m.HOME_ROUTES)
      },
      // Profile
      {
        path: 'profile',
        loadChildren: () => import('./features/profile/routes').then(m => m.PROFILE_ROUTES)
      },
      // Search
      {
        path: 'search',
        loadChildren: () => import('./features/search/routes').then(m => m.SEARCH_ROUTES)
      },
      // Administration
      {
        path: 'administration/users',
        loadChildren: () => import('./features/admin-users/routes').then(m => m.USERS_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'administration:users:read' }
      },
      {
        path: 'administration/groups',
        loadChildren: () => import('./features/admin-groups/routes').then(m => m.GROUPS_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'administration:groups:read' }
      },
      {
        path: 'administration/roles',
        loadChildren: () => import('./features/admin-roles/routes').then(m => m.ROLES_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'administration:roles:read' }
      },
      {
        path: 'administration/memberships',
        loadChildren: () => import('./features/admin-memberships/routes').then(m => m.MEMBERSHIPS_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'administration:memberships:read' }
      },
      {
        path: 'administration/responsabilities',
        loadChildren: () => import('./features/admin-responsibilities/routes').then(m => m.RESPONSIBILITIES_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'administration:responsabilities:read' }
      },
      // Content
      {
        path: 'content/articles',
        loadChildren: () => import('./features/content-articles/routes').then(m => m.ARTICLES_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'content:articles:read' }
      },
      {
        path: 'content/filemanager',
        loadChildren: () => import('./features/content-filemanager/routes').then(m => m.FILEMANAGER_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'content:filemanager:read' }
      },
      // System
      {
        path: 'system/dashboard',
        loadChildren: () => import('./features/system-dashboard/routes').then(m => m.DASHBOARD_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'system:dashboard:manager' }
      },
      {
        path: 'system/caches',
        loadChildren: () => import('./features/system-caches/routes').then(m => m.CACHES_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'system:caches:manager' }
      },
      {
        path: 'system/sessions',
        loadChildren: () => import('./features/system-sessions/routes').then(m => m.SESSIONS_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'system:sessions:manager' }
      },
      {
        path: 'system/loggers',
        loadChildren: () => import('./features/system-loggers/routes').then(m => m.LOGGERS_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'system:loggers:manager' }
      },
      {
        path: 'system/scheduler',
        loadChildren: () => import('./features/system-scheduler/routes').then(m => m.SCHEDULER_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'system:scheduler:manager' }
      },
      {
        path: 'system/liquibase',
        loadChildren: () => import('./features/system-liquibase/routes').then(m => m.LIQUIBASE_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'system:liquibase:manager' }
      },
      {
        path: 'system/bpmn',
        loadChildren: () => import('./features/system-bpmn/routes').then(m => m.BPMN_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'system:bpmn:manager' }
      },
      // Error pages
      {
        path: '403',
        component: ErrorPageComponent,
        data: { code: '403', message: 'Access Denied', icon: 'lock' }
      },
      {
        path: '**',
        component: ErrorPageComponent,
        data: { code: '404', message: 'Page not found', icon: 'error_outline' }
      }
    ]
  }
];
