import { Component, ChangeDetectionStrategy, ViewEncapsulation, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService, ConfigurationService } from '@blossom/core';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'blossom-login',
  standalone: true,
  imports: [FormsModule, RouterModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>Blossom</mat-card-title>
          <mat-card-subtitle>Sign in to your account</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          @if (error()) {
            <div class="error-message">{{ error() }}</div>
          }
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
            <button mat-flat-button color="primary" type="submit" class="full-width" [disabled]="loading()">
              {{ loading() ? 'Signing in...' : 'Sign In' }}
            </button>
          </form>
          <div class="forgot-link">
            <a routerLink="/forgotten-password">Forgot your password?</a>
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
    mat-card-header { margin-bottom: 24px; text-align: center; display: flex; flex-direction: column; align-items: center; }
    .login-card .mat-mdc-card-title { font-size: 28px; font-weight: 300; color: #2f4050; }
    .login-card .mat-mdc-card-subtitle { font-size: 14px; color: #999; }
    .login-card .mat-mdc-flat-button.mat-primary { background-color: #1ab394; font-size: 14px; height: 44px; }
    .forgot-link { text-align: center; margin-top: 16px; }
    .forgot-link a { color: #1ab394; text-decoration: none; font-size: 13px; }
  `]
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly configService = inject(ConfigurationService);
  private readonly router = inject(Router);

  username = '';
  password = '';
  readonly error = signal('');
  readonly loading = signal(false);

  login(): void {
    this.loading.set(true);
    this.error.set('');
    this.authService.login(this.username, this.password).pipe(
      switchMap(() => this.configService.load())
    ).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.error.set(err.status === 401 ? 'Invalid username or password' : 'Failed to load configuration');
        this.loading.set(false);
      }
    });
  }
}
