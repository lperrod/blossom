import { Routes } from '@angular/router';
import { UsersListComponent } from './users-list.component';
import { UserDetailComponent } from './user-detail.component';
export const USERS_ROUTES: Routes = [
  { path: '', component: UsersListComponent },
  { path: ':id', component: UserDetailComponent }
];
