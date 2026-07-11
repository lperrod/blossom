// Re-export all feature route configs
export { HOME_ROUTES } from './lib/home/routes';
export { PROFILE_ROUTES } from './lib/profile/routes';
export { SEARCH_ROUTES } from './lib/search/routes';
export { USERS_ROUTES } from './lib/admin-users/routes';
export { GROUPS_ROUTES } from './lib/admin-groups/routes';
export { ROLES_ROUTES } from './lib/admin-roles/routes';
export { MEMBERSHIPS_ROUTES } from './lib/admin-memberships/routes';
export { RESPONSIBILITIES_ROUTES } from './lib/admin-responsibilities/routes';
export { ARTICLES_ROUTES } from './lib/content-articles/routes';
export { FILEMANAGER_ROUTES } from './lib/content-filemanager/routes';
export { DASHBOARD_ROUTES } from './lib/system-dashboard/routes';
export { CACHES_ROUTES } from './lib/system-caches/routes';
export { SESSIONS_ROUTES } from './lib/system-sessions/routes';
export { LOGGERS_ROUTES } from './lib/system-loggers/routes';
export { SCHEDULER_ROUTES } from './lib/system-scheduler/routes';
export { LIQUIBASE_ROUTES } from './lib/system-liquibase/routes';
export { BPMN_ROUTES } from './lib/system-bpmn/routes';

// Export a helper that builds all default blossom routes (for use inside a LayoutComponent children)
import { Routes } from '@angular/router';
import { ErrorPageComponent } from '@blossom/shell';
import { privilegeGuard } from '@blossom/core';

export function blossomDefaultRoutes(): Routes {
  return [
    { path: '', loadChildren: () => import('./lib/home/routes').then(m => m.HOME_ROUTES) },
    { path: 'profile', loadChildren: () => import('./lib/profile/routes').then(m => m.PROFILE_ROUTES) },
    { path: 'search', loadChildren: () => import('./lib/search/routes').then(m => m.SEARCH_ROUTES) },
    { path: 'administration/users', loadChildren: () => import('./lib/admin-users/routes').then(m => m.USERS_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'administration:users:read' } },
    { path: 'administration/groups', loadChildren: () => import('./lib/admin-groups/routes').then(m => m.GROUPS_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'administration:groups:read' } },
    { path: 'administration/roles', loadChildren: () => import('./lib/admin-roles/routes').then(m => m.ROLES_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'administration:roles:read' } },
    { path: 'administration/memberships', loadChildren: () => import('./lib/admin-memberships/routes').then(m => m.MEMBERSHIPS_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'administration:memberships:read' } },
    { path: 'administration/responsabilities', loadChildren: () => import('./lib/admin-responsibilities/routes').then(m => m.RESPONSIBILITIES_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'administration:responsabilities:read' } },
    { path: 'content/articles', loadChildren: () => import('./lib/content-articles/routes').then(m => m.ARTICLES_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'content:articles:read' } },
    { path: 'content/filemanager', loadChildren: () => import('./lib/content-filemanager/routes').then(m => m.FILEMANAGER_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'content:filemanager:read' } },
    { path: 'system/dashboard', loadChildren: () => import('./lib/system-dashboard/routes').then(m => m.DASHBOARD_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'system:dashboard:manager' } },
    { path: 'system/caches', loadChildren: () => import('./lib/system-caches/routes').then(m => m.CACHES_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'system:caches:manager' } },
    { path: 'system/sessions', loadChildren: () => import('./lib/system-sessions/routes').then(m => m.SESSIONS_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'system:sessions:manager' } },
    { path: 'system/loggers', loadChildren: () => import('./lib/system-loggers/routes').then(m => m.LOGGERS_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'system:loggers:manager' } },
    { path: 'system/scheduler', loadChildren: () => import('./lib/system-scheduler/routes').then(m => m.SCHEDULER_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'system:scheduler:manager' } },
    { path: 'system/liquibase', loadChildren: () => import('./lib/system-liquibase/routes').then(m => m.LIQUIBASE_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'system:liquibase:manager' } },
    { path: 'system/bpmn', loadChildren: () => import('./lib/system-bpmn/routes').then(m => m.BPMN_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'system:bpmn:manager' } },
    { path: '403', component: ErrorPageComponent, data: { code: '403', message: 'Access Denied', icon: 'lock' } },
    { path: '**', component: ErrorPageComponent, data: { code: '404', message: 'Page not found', icon: 'error_outline' } },
  ];
}

// Export a full app routes builder (login + layout + all features)
import { LayoutComponent, LoginComponent, ForgottenPasswordComponent, ResetPasswordComponent, ActivationComponent } from '@blossom/shell';
import { authGuard } from '@blossom/core';

export function blossomAppRoutes(extraChildren: Routes = []): Routes {
  return [
    { path: 'login', component: LoginComponent },
    { path: 'forgotten-password', component: ForgottenPasswordComponent },
    { path: 'change-password', component: ResetPasswordComponent },
    { path: 'activate', component: ActivationComponent },
    {
      path: '',
      component: LayoutComponent,
      canActivate: [authGuard],
      children: [
        ...blossomDefaultRoutes().filter(r => r.path !== '**' && r.path !== '403'),
        ...extraChildren,
        // Error pages must be last
        { path: '403', component: ErrorPageComponent, data: { code: '403', message: 'Access Denied', icon: 'lock' } },
        { path: '**', component: ErrorPageComponent, data: { code: '404', message: 'Page not found', icon: 'error_outline' } },
      ]
    }
  ];
}
