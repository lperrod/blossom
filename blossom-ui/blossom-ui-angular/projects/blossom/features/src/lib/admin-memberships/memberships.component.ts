import { Component, OnInit, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { NotificationService } from '@blossom/core';
import { MembershipsService } from './memberships.service';
import { UsersService, UserDTO } from '../admin-users/users.service';
import { GroupsService, GroupDTO } from '../admin-groups/groups.service';

@Component({
  selector: 'app-memberships', standalone: true,
  imports: [FormsModule, MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2>User-Group Memberships</h2>
    <div class="controls">
      <mat-form-field appearance="outline"><mat-label>Select User</mat-label>
        <mat-select [(ngModel)]="selectedUserId" (selectionChange)="loadMemberships()">
          @for (u of users(); track u.id) {
            <mat-option [value]="u.id">{{ u.identifier }} - {{ u.firstname }} {{ u.lastname }}</mat-option>
          }
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Add to Group</mat-label>
        <mat-select [(ngModel)]="selectedGroupId">
          @for (g of groups(); track g.id) {
            <mat-option [value]="g.id">{{ g.name }}</mat-option>
          }
        </mat-select>
      </mat-form-field>
      <button mat-flat-button color="primary" (click)="associate()" [disabled]="!selectedUserId || !selectedGroupId"><mat-icon>link</mat-icon> Associate</button>
    </div>
    @if (memberships().length) {
      <table mat-table [dataSource]="memberships()" class="full-width">
        <ng-container matColumnDef="group"><th mat-header-cell *matHeaderCellDef>Group</th><td mat-cell *matCellDef="let m">{{ m.b?.name || m.id }}</td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let m"><button mat-icon-button color="warn" (click)="dissociate(m.id)"><mat-icon>link_off</mat-icon></button></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="['group','actions']"></tr>
        <tr mat-row *matRowDef="let row; columns: ['group','actions'];"></tr>
      </table>
    }
  `,
  styles: [`.controls{display:flex;gap:16px;align-items:baseline;flex-wrap:wrap;margin-bottom:16px;}.full-width{width:100%;}`]
})
export class MembershipsComponent implements OnInit {
  users = signal<UserDTO[]>([]); groups = signal<GroupDTO[]>([]); memberships = signal<any[]>([]);
  selectedUserId: number | null = null; selectedGroupId: number | null = null;

  private svc = inject(MembershipsService);
  private usersSvc = inject(UsersService);
  private groupsSvc = inject(GroupsService);
  private notify = inject(NotificationService);

  ngOnInit(): void {
    this.usersSvc.list('', 0, 1000).subscribe(p => this.users.set(p.content));
    this.groupsSvc.list('', 0, 1000).subscribe(p => this.groups.set(p.content));
  }
  loadMemberships(): void { if (this.selectedUserId) this.svc.getByUser(this.selectedUserId).subscribe(m => this.memberships.set(m)); }
  associate(): void { if (this.selectedUserId && this.selectedGroupId) this.svc.associate(this.selectedUserId, this.selectedGroupId).subscribe({ next: () => { this.notify.success('Associated'); this.loadMemberships(); }, error: () => this.notify.error('Failed') }); }
  dissociate(id: string): void { this.svc.dissociate(id).subscribe(() => { this.notify.success('Dissociated'); this.loadMemberships(); }); }
}
