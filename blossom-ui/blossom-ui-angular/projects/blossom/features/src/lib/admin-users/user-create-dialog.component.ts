import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { NotificationService } from '@blossom/core';
import { UsersService } from './users.service';

@Component({
  selector: 'app-user-create-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>Create User</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="full-width"><mat-label>Identifier</mat-label><input matInput [(ngModel)]="form.identifier" required></mat-form-field>
      <mat-form-field appearance="outline" class="full-width"><mat-label>Password</mat-label><input matInput type="password" [(ngModel)]="form.password" required></mat-form-field>
      <mat-form-field appearance="outline" class="full-width"><mat-label>First Name</mat-label><input matInput [(ngModel)]="form.firstname"></mat-form-field>
      <mat-form-field appearance="outline" class="full-width"><mat-label>Last Name</mat-label><input matInput [(ngModel)]="form.lastname"></mat-form-field>
      <mat-form-field appearance="outline" class="full-width"><mat-label>Email</mat-label><input matInput [(ngModel)]="form.email" type="email"></mat-form-field>
      <mat-form-field appearance="outline" class="full-width"><mat-label>Civility</mat-label>
        <mat-select [(ngModel)]="form.civility"><mat-option value="MAN">Mr.</mat-option><mat-option value="WOMAN">Mrs.</mat-option></mat-select>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close()">Cancel</button>
      <button mat-flat-button color="primary" (click)="create()" [disabled]="!form.identifier">Create</button>
    </mat-dialog-actions>
  `,
  styles: [`.full-width { width:100%; }`]
})
export class UserCreateDialogComponent {
  form: any = { identifier: '', password: '', firstname: '', lastname: '', email: '', civility: 'MAN' };

  constructor(public dialogRef: MatDialogRef<UserCreateDialogComponent>, private usersService: UsersService, private notify: NotificationService) {}

  create(): void {
    this.usersService.create(this.form).subscribe({
      next: (user) => { this.notify.success('User created'); this.dialogRef.close(user); },
      error: () => this.notify.error('Failed to create user')
    });
  }
}
