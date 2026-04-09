import { Routes } from '@angular/router';
import { RolesListComponent } from './roles-list.component';
import { RoleDetailComponent } from './role-detail.component';
export const ROLES_ROUTES: Routes = [{ path: '', component: RolesListComponent }, { path: ':id', component: RoleDetailComponent }];
