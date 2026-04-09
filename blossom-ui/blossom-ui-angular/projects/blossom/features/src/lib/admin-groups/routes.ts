import { Routes } from '@angular/router';
import { GroupsListComponent } from './groups-list.component';
import { GroupDetailComponent } from './group-detail.component';
export const GROUPS_ROUTES: Routes = [{ path: '', component: GroupsListComponent }, { path: ':id', component: GroupDetailComponent }];
