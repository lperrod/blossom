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
import { MatSelectModule } from '@angular/material/select';
import { MatDialog } from '@angular/material/dialog';
import { NotificationService } from '@blossom/core';
import { ConfirmDialogComponent } from '@blossom/ui';
import { UsersService, UserDTO } from './users.service';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatTabsModule, MatSelectModule],
  template: `
    <div class="page-header" *ngIf="user">
      <h2>{{ user.firstname }} {{ user.lastname }}</h2>
      <div>
        <button mat-flat-button color="primary" (click)="save()" *ngIf="editing"><mat-icon>save</mat-icon> Save</button>
        <button mat-button (click)="editing = !editing">{{ editing ? 'Cancel' : 'Edit' }}</button>
        <button mat-icon-button color="warn" (click)="confirmDelete()"><mat-icon>delete</mat-icon></button>
      </div>
    </div>
    <mat-tab-group *ngIf="user">
      <mat-tab label="Information">
        <div class="tab-content">
          <mat-card>
            <mat-card-content>
              <div class="form-grid">
                <mat-form-field appearance="outline"><mat-label>Identifier</mat-label><input matInput [(ngModel)]="user.identifier" [disabled]="!editing"></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Civility</mat-label>
                  <mat-select [(ngModel)]="user.civility" [disabled]="!editing"><mat-option value="MAN">Mr.</mat-option><mat-option value="WOMAN">Mrs.</mat-option></mat-select>
                </mat-form-field>
                <mat-form-field appearance="outline"><mat-label>First Name</mat-label><input matInput [(ngModel)]="user.firstname" [disabled]="!editing"></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Last Name</mat-label><input matInput [(ngModel)]="user.lastname" [disabled]="!editing"></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Email</mat-label><input matInput [(ngModel)]="user.email" [disabled]="!editing"></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Phone</mat-label><input matInput [(ngModel)]="user.phone" [disabled]="!editing"></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Company</mat-label><input matInput [(ngModel)]="user.company" [disabled]="!editing"></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Function</mat-label><input matInput [(ngModel)]="user.function" [disabled]="!editing"></mat-form-field>
                <mat-form-field appearance="outline" class="full-span"><mat-label>Description</mat-label><textarea matInput [(ngModel)]="user.description" [disabled]="!editing" rows="3"></textarea></mat-form-field>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </mat-tab>
    </mat-tab-group>
  `,
  styles: [`
    .page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
    .tab-content { padding:16px 0; }
    .form-grid { display:grid; grid-template-columns:repeat(2,1fr); gap:8px 16px; }
    .full-span { grid-column:span 2; }
  `]
})
export class UserDetailComponent implements OnInit {
  user: UserDTO | null = null;
  editing = false;

  constructor(private route: ActivatedRoute, private router: Router, private usersService: UsersService, private dialog: MatDialog, private notify: NotificationService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.usersService.get(id).subscribe(u => this.user = u);
  }

  save(): void {
    if (!this.user) return;
    this.usersService.update(this.user.id, this.user).subscribe({
      next: u => { this.user = u; this.editing = false; this.notify.success('User updated'); },
      error: () => this.notify.error('Failed to update user')
    });
  }

  confirmDelete(): void {
    if (!this.user) return;
    this.dialog.open(ConfirmDialogComponent, { data: { title: 'Delete User', message: `Delete user ${this.user.identifier}?`, confirmText: 'Delete' } })
      .afterClosed().subscribe(r => {
        if (r) this.usersService.delete(this.user!.id).subscribe({
          next: () => { this.notify.success('User deleted'); this.router.navigate(['/administration/users']); },
          error: () => this.notify.error('Failed to delete user')
        });
      });
  }
}
