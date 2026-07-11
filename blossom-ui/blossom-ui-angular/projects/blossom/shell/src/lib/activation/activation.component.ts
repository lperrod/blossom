import { Component, OnInit, OnDestroy, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivationService } from '@blossom/core';

@Component({
  selector: 'blossom-activation',
  standalone: true,
  imports: [CommonModule, RouterModule, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>Blossom</mat-card-title>
          <mat-card-subtitle>Account Activation</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div *ngIf="loading" class="center">
            <mat-spinner diameter="40"></mat-spinner>
            <p>Activating your account...</p>
          </div>
          <div *ngIf="error" class="error-message">
            {{ error }}
            <div class="back-link"><a routerLink="/login">Back to Sign In</a></div>
          </div>
          <div *ngIf="success" class="success-message">
            Your account has been activated. Please set your password.
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #2f4050; }
    .login-card { width: 400px; padding: 32px; border-radius: 4px; }
    .center { text-align: center; }
    .center p { color: #666; margin-top: 16px; }
    .error-message { background: #ed5565; color: white; padding: 12px; border-radius: 4px; margin-bottom: 16px; font-size: 13px; }
    .success-message { background: #1ab394; color: white; padding: 12px; border-radius: 4px; margin-bottom: 16px; font-size: 13px; }
    .back-link { margin-top: 8px; }
    .back-link a { color: white; text-decoration: underline; }
    mat-card-header { margin-bottom: 24px; text-align: center; display: flex; flex-direction: column; align-items: center; }
    .login-card .mat-mdc-card-title { font-size: 28px; font-weight: 300; color: #2f4050; }
    .login-card .mat-mdc-card-subtitle { font-size: 14px; color: #999; }
  `]
})
export class ActivationComponent implements OnInit, OnDestroy {
  loading = true;
  error = '';
  success = false;
  private redirectTimer: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private activationService: ActivationService
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!token) {
      this.loading = false;
      this.error = 'Invalid activation link.';
      return;
    }
    this.activationService.activate(token).subscribe({
      next: (result) => {
        this.loading = false;
        this.success = true;
        // Redirect to password reset with the token
        this.redirectTimer = setTimeout(() => {
          this.router.navigate(['/change-password'], { queryParams: { token: result.resetToken } });
        }, 2000);
      },
      error: () => {
        this.loading = false;
        this.error = 'Activation failed. The link may be invalid or expired.';
      }
    });
  }

  ngOnDestroy(): void {
    if (this.redirectTimer) clearTimeout(this.redirectTimer);
  }
}
