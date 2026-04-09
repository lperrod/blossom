import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { SearchBarComponent } from '@blossom/ui';
import { NotificationService } from '@blossom/core';
import { UsersService, UserDTO } from './users.service';
import { UserCreateDialogComponent } from './user-create-dialog.component';

@Component({
  selector: 'app-users-list',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule, MatChipsModule, SearchBarComponent],
  template: `
    <div class="page-header">
      <h2>Users</h2>
      <button mat-flat-button color="primary" (click)="openCreateDialog()"><mat-icon>add</mat-icon> Create User</button>
    </div>
    <blossom-search-bar (search)="onSearch($event)"></blossom-search-bar>
    <table mat-table [dataSource]="users" class="full-width">
      <ng-container matColumnDef="identifier"><th mat-header-cell *matHeaderCellDef>Identifier</th><td mat-cell *matCellDef="let u">{{ u.identifier }}</td></ng-container>
      <ng-container matColumnDef="firstname"><th mat-header-cell *matHeaderCellDef>First Name</th><td mat-cell *matCellDef="let u">{{ u.firstname }}</td></ng-container>
      <ng-container matColumnDef="lastname"><th mat-header-cell *matHeaderCellDef>Last Name</th><td mat-cell *matCellDef="let u">{{ u.lastname }}</td></ng-container>
      <ng-container matColumnDef="email"><th mat-header-cell *matHeaderCellDef>Email</th><td mat-cell *matCellDef="let u">{{ u.email }}</td></ng-container>
      <ng-container matColumnDef="activated"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let u"><mat-chip [color]="u.activated ? 'primary' : 'warn'" selected>{{ u.activated ? 'Active' : 'Inactive' }}</mat-chip></td></ng-container>
      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns;" (click)="goToDetail(row.id)" class="clickable"></tr>
    </table>
    <mat-paginator [length]="totalElements" [pageSize]="25" (page)="onPage($event)" showFirstLastButtons></mat-paginator>
  `,
  styles: [`.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; } .full-width { width:100%; } .clickable { cursor:pointer; } .clickable:hover { background:rgba(0,0,0,0.04); }`]
})
export class UsersListComponent implements OnInit {
  users: UserDTO[] = [];
  columns = ['identifier', 'firstname', 'lastname', 'email', 'activated'];
  totalElements = 0;
  private query = '';
  private page = 0;

  constructor(private usersService: UsersService, private router: Router, private dialog: MatDialog, private notify: NotificationService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.usersService.list(this.query, this.page).subscribe(p => { this.users = p.content; this.totalElements = p.totalElements; });
  }

  onSearch(q: string): void { this.query = q; this.page = 0; this.load(); }
  onPage(e: PageEvent): void { this.page = e.pageIndex; this.load(); }
  goToDetail(id: number): void { this.router.navigate(['/administration/users', id]); }

  openCreateDialog(): void {
    this.dialog.open(UserCreateDialogComponent, { width: '500px' }).afterClosed().subscribe(r => { if (r) this.load(); });
  }
}
