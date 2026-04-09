import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService, ConfigurationService } from '@blossom/core';

@Component({
  selector: 'blossom-login',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>Blossom</mat-card-title>
          <mat-card-subtitle>Sign in to your account</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div *ngIf="error" class="error-message">{{ error }}</div>
          <form (ngSubmit)="login()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Username</mat-label>
              <input matInput [(ngModel)]="username" name="username" required>
              <mat-icon matSuffix>person</mat-icon>
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput [(ngModel)]="password" name="password" type="password" required>
              <mat-icon matSuffix>lock</mat-icon>
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit" class="full-width" [disabled]="loading">
              {{ loading ? 'Signing in...' : 'Sign In' }}
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
    mat-card-header { margin-bottom: 24px; text-align: center; display: flex; flex-direction: column; align-items: center; }
    ::ng-deep .login-card .mat-mdc-card-title { font-size: 28px; font-weight: 300; color: #2f4050; }
    ::ng-deep .login-card .mat-mdc-card-subtitle { font-size: 14px; color: #999; }
    ::ng-deep .login-card .mat-mdc-flat-button.mat-primary { background-color: #1ab394; font-size: 14px; height: 44px; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private configService: ConfigurationService,
    private router: Router
  ) {}

  login(): void {
    this.loading = true;
    this.error = '';
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.configService.load().subscribe({
          next: () => this.router.navigate(['/']),
          error: () => {
            this.error = 'Failed to load configuration';
            this.loading = false;
          }
        });
      },
      error: () => {
        this.error = 'Invalid username or password';
        this.loading = false;
      }
    });
  }
}
