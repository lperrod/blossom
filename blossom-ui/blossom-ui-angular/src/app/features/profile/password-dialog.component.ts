import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { NotificationService } from '@blossom/core';
import { ProfileService } from './profile.service';

@Component({
  selector: 'app-password-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Change Password</h2>
    <mat-dialog-content>
      <div *ngIf="error" class="error">{{ error }}</div>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>New Password</mat-label>
        <input matInput type="password" [(ngModel)]="password">
      </mat-form-field>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Confirm Password</mat-label>
        <input matInput type="password" [(ngModel)]="passwordRepeater">
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close()">Cancel</button>
      <button mat-flat-button color="primary" (click)="save()" [disabled]="!password || password !== passwordRepeater">Save</button>
    </mat-dialog-actions>
  `,
  styles: [`.full-width { width: 100%; } .error { background: #f44336; color: white; padding: 8px; border-radius: 4px; margin-bottom: 16px; }`]
})
export class PasswordDialogComponent {
  password = '';
  passwordRepeater = '';
  error = '';

  constructor(
    public dialogRef: MatDialogRef<PasswordDialogComponent>,
    private profileService: ProfileService,
    private notify: NotificationService
  ) {}

  save(): void {
    this.profileService.updatePassword(this.password, this.passwordRepeater).subscribe({
      next: () => { this.notify.success('Password updated'); this.dialogRef.close(); },
      error: (err) => { this.error = err.error?.message || 'Failed to update password'; }
    });
  }
}
