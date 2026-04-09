import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { NotificationService } from '@blossom/core';
import { ProfileService } from './profile.service';
import { PasswordDialogComponent } from './password-dialog.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatDividerModule, MatDialogModule],
  template: `
    <h2>My Profile</h2>
    <mat-card *ngIf="profile">
      <mat-card-header>
        <mat-icon mat-card-avatar style="font-size:40px;width:40px;height:40px;">account_circle</mat-icon>
        <mat-card-title>{{ profile.firstname }} {{ profile.lastname }}</mat-card-title>
        <mat-card-subtitle>{{ profile.identifier }}</mat-card-subtitle>
      </mat-card-header>
      <mat-card-content>
        <div class="profile-grid">
          <div class="field"><label>Email</label><span>{{ profile.email }}</span></div>
          <div class="field"><label>Phone</label><span>{{ profile.phone || '-' }}</span></div>
          <div class="field"><label>Company</label><span>{{ profile.company || '-' }}</span></div>
          <div class="field"><label>Function</label><span>{{ profile.function || '-' }}</span></div>
          <div class="field"><label>Locale</label><span>{{ profile.locale }}</span></div>
          <div class="field"><label>Last Connection</label><span>{{ profile.lastConnection | date:'medium' }}</span></div>
        </div>
      </mat-card-content>
      <mat-divider></mat-divider>
      <mat-card-actions>
        <button mat-flat-button color="primary" (click)="openPasswordDialog()">
          <mat-icon>lock</mat-icon> Change Password
        </button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .profile-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; padding: 16px 0; }
    .field label { display: block; font-size: 12px; color: #666; margin-bottom: 4px; }
    .field span { font-size: 14px; }
  `]
})
export class ProfileComponent implements OnInit {
  profile: any;

  constructor(private profileService: ProfileService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.profileService.get().subscribe(p => this.profile = p);
  }

  openPasswordDialog(): void {
    this.dialog.open(PasswordDialogComponent, { width: '400px' });
  }
}
