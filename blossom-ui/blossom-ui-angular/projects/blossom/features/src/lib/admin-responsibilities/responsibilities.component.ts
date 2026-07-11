import { Component, OnInit, ChangeDetectionStrategy, inject, signal } from '@angular/core';
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
  imports: [FormsModule, MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatSelectModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2>User-Role Responsibilities</h2>
    <div class="controls">
      <mat-form-field appearance="outline"><mat-label>Select User</mat-label>
        <mat-select [(ngModel)]="selectedUserId" (selectionChange)="loadResponsibilities()">
          @for (u of users(); track u.id) {
            <mat-option [value]="u.id">{{ u.identifier }} - {{ u.firstname }} {{ u.lastname }}</mat-option>
          }
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Add Role</mat-label>
        <mat-select [(ngModel)]="selectedRoleId">
          @for (r of roles(); track r.id) {
            <mat-option [value]="r.id">{{ r.name }}</mat-option>
          }
        </mat-select>
      </mat-form-field>
      <button mat-flat-button color="primary" (click)="associate()" [disabled]="!selectedUserId || !selectedRoleId"><mat-icon>link</mat-icon> Associate</button>
    </div>
    @if (responsibilities().length) {
      <table mat-table [dataSource]="responsibilities()" class="full-width">
        <ng-container matColumnDef="role"><th mat-header-cell *matHeaderCellDef>Role</th><td mat-cell *matCellDef="let r">{{ r.b?.name || r.id }}</td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let r"><button mat-icon-button color="warn" (click)="dissociate(r.id)"><mat-icon>link_off</mat-icon></button></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="['role','actions']"></tr>
        <tr mat-row *matRowDef="let row; columns: ['role','actions'];"></tr>
      </table>
    }
  `,
  styles: [`.controls{display:flex;gap:16px;align-items:baseline;flex-wrap:wrap;margin-bottom:16px;}.full-width{width:100%;}`]
})
export class ResponsibilitiesComponent implements OnInit {
  users = signal<UserDTO[]>([]); roles = signal<RoleDTO[]>([]); responsibilities = signal<any[]>([]);
  selectedUserId: number | null = null; selectedRoleId: number | null = null;

  private svc = inject(ResponsibilitiesService);
  private usersSvc = inject(UsersService);
  private rolesSvc = inject(RolesService);
  private notify = inject(NotificationService);

  ngOnInit(): void {
    this.usersSvc.list('', 0, 1000).subscribe(p => this.users.set(p.content));
    this.rolesSvc.list('', 0, 1000).subscribe(p => this.roles.set(p.content));
  }
  loadResponsibilities(): void { if (this.selectedUserId) this.svc.getByUser(this.selectedUserId).subscribe(r => this.responsibilities.set(r)); }
  associate(): void { if (this.selectedUserId && this.selectedRoleId) this.svc.associate(this.selectedUserId, this.selectedRoleId).subscribe({ next: () => { this.notify.success('Associated'); this.loadResponsibilities(); }, error: () => this.notify.error('Failed') }); }
  dissociate(id: string): void { this.svc.dissociate(id).subscribe(() => { this.notify.success('Dissociated'); this.loadResponsibilities(); }); }
}
