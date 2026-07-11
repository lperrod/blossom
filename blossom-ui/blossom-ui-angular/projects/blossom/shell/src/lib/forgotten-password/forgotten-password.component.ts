import { Component, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ActivationService } from '@blossom/core';

@Component({
  selector: 'blossom-forgotten-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>Blossom</mat-card-title>
          <mat-card-subtitle>Reset your password</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div *ngIf="success" class="success-message">
            If an account exists with this identifier, a password reset email has been sent.
          </div>
          <div *ngIf="error" class="error-message">{{ error }}</div>
          <form *ngIf="!success" (ngSubmit)="submit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Username or Email</mat-label>
              <input matInput [(ngModel)]="loginOrEmail" name="loginOrEmail" required>
              <mat-icon matSuffix>person</mat-icon>
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit" class="full-width" [disabled]="loading || !loginOrEmail">
              {{ loading ? 'Sending...' : 'Send Reset Link' }}
            </button>
          </form>
          <div class="back-link">
            <a routerLink="/login">Back to Sign In</a>
          </div>
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
    .back-link { text-align: center; margin-top: 16px; }
    .back-link a { color: #1ab394; text-decoration: none; }
    mat-card-header { margin-bottom: 24px; text-align: center; display: flex; flex-direction: column; align-items: center; }
    .login-card .mat-mdc-card-title { font-size: 28px; font-weight: 300; color: #2f4050; }
    .login-card .mat-mdc-card-subtitle { font-size: 14px; color: #999; }
    .login-card .mat-mdc-flat-button.mat-primary { background-color: #1ab394; font-size: 14px; height: 44px; }
  `]
})
export class ForgottenPasswordComponent {
  loginOrEmail = '';
  error = '';
  success = false;
  loading = false;

  constructor(private activationService: ActivationService) {}

  submit(): void {
    this.loading = true;
    this.error = '';
    this.activationService.requestPasswordReset(this.loginOrEmail).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
      },
      error: () => {
        // Always show success to avoid leaking user existence
        this.success = true;
        this.loading = false;
      }
    });
  }
}
