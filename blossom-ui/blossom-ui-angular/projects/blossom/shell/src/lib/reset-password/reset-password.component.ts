import { Component, ChangeDetectionStrategy, ViewEncapsulation, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ActivationService } from '@blossom/core';

@Component({
  selector: 'blossom-reset-password',
  standalone: true,
  imports: [FormsModule, RouterModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>Blossom</mat-card-title>
          <mat-card-subtitle>Set your new password</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          @if (tokenInvalid()) {
            <div class="error-message">
              This password reset link is invalid or has expired.
              <div class="back-link"><a routerLink="/forgotten-password">Request a new one</a></div>
            </div>
          }
          @if (success()) {
            <div class="success-message">
              Your password has been changed successfully.
              <div class="back-link"><a routerLink="/login">Sign In</a></div>
            </div>
          }
          @if (error()) {
            <div class="error-message">{{ error() }}</div>
          }
          @if (!tokenInvalid() && !success() && tokenValidated()) {
            <form (ngSubmit)="submit()">
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
              @if (password && passwordRepeater && password !== passwordRepeater) {
                <div class="validation-error">
                  Passwords do not match
                </div>
              }
              @if (password && password.length < 8) {
                <div class="validation-error">
                  Password must be at least 8 characters
                </div>
              }
              <button mat-flat-button color="primary" type="submit" class="full-width"
                [disabled]="loading() || !password || !passwordRepeater || password !== passwordRepeater || password.length < 8">
                {{ loading() ? 'Changing...' : 'Change Password' }}
              </button>
            </form>
          }
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
export class ResetPasswordComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly activationService = inject(ActivationService);

  password = '';
  passwordRepeater = '';
  readonly error = signal('');
  readonly success = signal(false);
  readonly loading = signal(false);
  readonly tokenInvalid = signal(false);
  readonly tokenValidated = signal(false);

  private readonly token: string;

  constructor() {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.tokenInvalid.set(true);
      return;
    }
    this.activationService.validateToken(this.token).subscribe({
      next: (result) => {
        if (result.valid) {
          this.tokenValidated.set(true);
        } else {
          this.tokenInvalid.set(true);
        }
      },
      error: () => this.tokenInvalid.set(true)
    });
  }

  submit(): void {
    this.loading.set(true);
    this.error.set('');
    this.activationService.changePassword(this.token, this.password, this.passwordRepeater).subscribe({
      next: () => {
        this.success.set(true);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Failed to change password. Please try again.');
        this.loading.set(false);
      }
    });
  }
}
