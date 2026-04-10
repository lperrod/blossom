import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { NotificationService } from '@blossom/core';
import { ResponsibilitiesService } from './responsibilities.service';
import { UsersService, UserDTO } from '../admin-users/users.service';
import { RolesService, RoleDTO } from '../admin-roles/roles.service';

@Component({
  selector: 'app-responsibilities', standalone: true,
  imports: [CommonModule, FormsModule, MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatSelectModule],
  template: `
    <h2>User-Role Responsibilities</h2>
    <div class="controls">
      <mat-form-field appearance="outline"><mat-label>Select User</mat-label>
        <mat-select [(ngModel)]="selectedUserId" (selectionChange)="loadResponsibilities()">
          <mat-option *ngFor="let u of users" [value]="u.id">{{ u.identifier }} - {{ u.firstname }} {{ u.lastname }}</mat-option>
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Add Role</mat-label>
        <mat-select [(ngModel)]="selectedRoleId">
          <mat-option *ngFor="let r of roles" [value]="r.id">{{ r.name }}</mat-option>
        </mat-select>
      </mat-form-field>
      <button mat-flat-button color="primary" (click)="associate()" [disabled]="!selectedUserId || !selectedRoleId"><mat-icon>link</mat-icon> Associate</button>
    </div>
    <table mat-table [dataSource]="responsibilities" class="full-width" *ngIf="responsibilities.length">
      <ng-container matColumnDef="role"><th mat-header-cell *matHeaderCellDef>Role</th><td mat-cell *matCellDef="let r">{{ r.b?.name || r.id }}</td></ng-container>
      <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let r"><button mat-icon-button color="warn" (click)="dissociate(r.id)"><mat-icon>link_off</mat-icon></button></td></ng-container>
      <tr mat-header-row *matHeaderRowDef="['role','actions']"></tr>
      <tr mat-row *matRowDef="let row; columns: ['role','actions'];"></tr>
    </table>
  `,
  styles: [`.controls{display:flex;gap:16px;align-items:baseline;flex-wrap:wrap;margin-bottom:16px;}.full-width{width:100%;}`]
})
export class ResponsibilitiesComponent implements OnInit {
  users: UserDTO[] = []; roles: RoleDTO[] = []; responsibilities: any[] = [];
  selectedUserId: number | null = null; selectedRoleId: number | null = null;
  constructor(private svc: ResponsibilitiesService, private usersSvc: UsersService, private rolesSvc: RolesService, private notify: NotificationService) {}
  ngOnInit(): void {
    this.usersSvc.list('', 0, 1000).subscribe(p => this.users = p.content);
    this.rolesSvc.list('', 0, 1000).subscribe(p => this.roles = p.content);
  }
  loadResponsibilities(): void { if (this.selectedUserId) this.svc.getByUser(this.selectedUserId).subscribe(r => this.responsibilities = r); }
  associate(): void { if (this.selectedUserId && this.selectedRoleId) this.svc.associate(this.selectedUserId, this.selectedRoleId).subscribe({ next: () => { this.notify.success('Associated'); this.loadResponsibilities(); }, error: () => this.notify.error('Failed') }); }
  dissociate(id: string): void { this.svc.dissociate(id).subscribe(() => { this.notify.success('Dissociated'); this.loadResponsibilities(); }); }
}
