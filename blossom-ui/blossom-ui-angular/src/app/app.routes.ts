import { Routes } from '@angular/router';
import { blossomAppRoutes } from '@blossom/features';

// Use blossomAppRoutes() for all default blossom pages.
// Pass extra children routes to add custom pages:
//
//   blossomAppRoutes([
//     { path: 'my-feature', loadChildren: () => import('./my-feature/routes').then(m => m.MY_ROUTES) }
//   ])
//
export const routes: Routes = blossomAppRoutes();
