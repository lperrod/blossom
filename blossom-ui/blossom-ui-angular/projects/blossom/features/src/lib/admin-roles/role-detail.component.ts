import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialog } from '@angular/material/dialog';
import { NotificationService } from '@blossom/core';
import { ConfirmDialogComponent, PrivilegeTreeComponent } from '@blossom/ui';
import { RolesService, RoleDTO } from './roles.service';

@Component({
  selector: 'app-role-detail', standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatTabsModule, PrivilegeTreeComponent],
  template: `
    <div class="page-header" *ngIf="role"><h2>{{ role.name }}</h2>
      <div><button mat-flat-button color="primary" (click)="save()" *ngIf="editing"><mat-icon>save</mat-icon> Save</button>
      <button mat-button (click)="editing=!editing">{{ editing ? 'Cancel' : 'Edit' }}</button>
      <button mat-icon-button color="warn" (click)="confirmDelete()" aria-label="Delete"><mat-icon>delete</mat-icon></button></div>
    </div>
    <mat-tab-group *ngIf="role">
      <mat-tab label="Information"><div class="tab-content"><mat-card><mat-card-content>
        <mat-form-field appearance="outline" class="full-width"><mat-label>Name</mat-label><input matInput [(ngModel)]="role.name" [disabled]="!editing"></mat-form-field>
        <mat-form-field appearance="outline" class="full-width"><mat-label>Description</mat-label><textarea matInput [(ngModel)]="role.description" [disabled]="!editing" rows="3"></textarea></mat-form-field>
      </mat-card-content></mat-card></div></mat-tab>
      <mat-tab label="Privileges"><div class="tab-content">
        <blossom-privilege-tree [privileges]="availablePrivileges" [selected]="role.privileges || []" (selectionChange)="onPrivilegesChange($event)"></blossom-privilege-tree>
      </div></mat-tab>
    </mat-tab-group>
  `,
  styles: [`.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;}.tab-content{padding:16px 0;}.full-width{width:100%;}`]
})
export class RoleDetailComponent implements OnInit {
  role: RoleDTO | null = null; editing = false; availablePrivileges: any[] = [];
  constructor(private route: ActivatedRoute, private router: Router, private svc: RolesService, private dialog: MatDialog, private notify: NotificationService) {}
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.svc.get(id).subscribe(r => this.role = r);
    this.svc.getPrivileges().subscribe(p => this.availablePrivileges = p);
  }
  onPrivilegesChange(privs: string[]): void { if (this.role) this.role.privileges = privs; }
  save(): void { if (!this.role) return; this.svc.update(this.role.id, this.role).subscribe({ next: r => { this.role = r; this.editing = false; this.notify.success('Role updated'); }, error: () => this.notify.error('Failed') }); }
  confirmDelete(): void { if (!this.role) return; this.dialog.open(ConfirmDialogComponent, { data: { title: 'Delete Role', message: `Delete ${this.role.name}?`, confirmText: 'Delete' } }).afterClosed().subscribe(r => { if (r) this.svc.delete(this.role!.id).subscribe({ next: () => { this.notify.success('Role deleted'); this.router.navigate(['/administration/roles']); } }); }); }
}
