import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ActivationService } from '@blossom/core';

@Component({
  selector: 'blossom-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>Blossom</mat-card-title>
          <mat-card-subtitle>Set your new password</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div *ngIf="tokenInvalid" class="error-message">
            This password reset link is invalid or has expired.
            <div class="back-link"><a routerLink="/forgotten-password">Request a new one</a></div>
          </div>
          <div *ngIf="success" class="success-message">
            Your password has been changed successfully.
            <div class="back-link"><a routerLink="/login">Sign In</a></div>
          </div>
          <div *ngIf="error" class="error-message">{{ error }}</div>
          <form *ngIf="!tokenInvalid && !success && tokenValidated" (ngSubmit)="submit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>New Password</mat-label>
              <input matInput [(ngModel)]="password" name="password" type="password" required minlength="8">
              <mat-icon matSuffix>lock</mat-icon>
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Confirm Password</mat-label>
              <input matInput [(ngModel)]="passwordRepeater" name="passwordRepeater" type="password" required>
              <mat-icon matSuffix>lock</mat-icon>
            </mat-form-field>
            <div *ngIf="password && passwordRepeater && password !== passwordRepeater" class="validation-error">
              Passwords do not match
            </div>
            <div *ngIf="password && password.length < 8" class="validation-error">
              Password must be at least 8 characters
            </div>
            <button mat-flat-button color="primary" type="submit" class="full-width"
              [disabled]="loading || !password || !passwordRepeater || password !== passwordRepeater || password.length < 8">
              {{ loading ? 'Changing...' : 'Change Password' }}
            </button>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #2f4050; }
    .login-card { width: 400px; padding: 32px; border-radius: 4px; }
    .full-width { width: 100%; margin-bottom: 8px; }
    .error-message { background: #ed5565; color: white; padding: 12px; border-radius: 4px; margin-bottom: 16px; font-size: 13px; }
    .success-message { background: #1ab394; color: white; padding: 12px; border-radius: 4px; margin-bottom: 16px; font-size: 13px; }
    .validation-error { color: #ed5565; font-size: 12px; margin-bottom: 8px; }
    .back-link { margin-top: 8px; }
    .back-link a { color: white; text-decoration: underline; }
    mat-card-header { margin-bottom: 24px; text-align: center; display: flex; flex-direction: column; align-items: center; }
    .login-card .mat-mdc-card-title { font-size: 28px; font-weight: 300; color: #2f4050; }
    .login-card .mat-mdc-card-subtitle { font-size: 14px; color: #999; }
    .login-card .mat-mdc-flat-button.mat-primary { background-color: #1ab394; font-size: 14px; height: 44px; }
  `]
})
export class ResetPasswordComponent implements OnInit {
  token = '';
  password = '';
  passwordRepeater = '';
  error = '';
  success = false;
  loading = false;
  tokenInvalid = false;
  tokenValidated = false;

  constructor(
    private route: ActivatedRoute,
    private activationService: ActivationService
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.tokenInvalid = true;
      return;
    }
    this.activationService.validateToken(this.token).subscribe({
      next: (result) => {
        if (result.valid) {
          this.tokenValidated = true;
        } else {
          this.tokenInvalid = true;
        }
      },
      error: () => this.tokenInvalid = true
    });
  }

  submit(): void {
    this.loading = true;
    this.error = '';
    this.activationService.changePassword(this.token, this.password, this.passwordRepeater).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to change password. Please try again.';
        this.loading = false;
      }
    });
  }
}
