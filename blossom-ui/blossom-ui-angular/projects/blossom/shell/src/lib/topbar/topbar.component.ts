import { Component, ChangeDetectionStrategy, inject, computed } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService, ConfigurationService } from '@blossom/core';

@Component({
  selector: 'blossom-topbar',
  standalone: true,
  imports: [RouterModule, MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-toolbar class="blossom-topbar">
      <span class="spacer"></span>
      @if (user()) {
        <button mat-icon-button [matMenuTriggerFor]="userMenu" class="user-btn" aria-label="User menu">
          <mat-icon>account_circle</mat-icon>
          <span class="user-name">{{ user()!.firstname }} {{ user()!.lastname }}</span>
        </button>
      }
      <mat-menu #userMenu="matMenu">
        @if (user()) {
          <div class="user-info">
            <strong>{{ user()!.firstname }} {{ user()!.lastname }}</strong>
            <small>{{ user()!.email || user()!.identifier }}</small>
          </div>
        }
        <button mat-menu-item routerLink="/profile">
          <mat-icon>person</mat-icon>
          <span>Profile</span>
        </button>
        <button mat-menu-item (click)="logout()">
          <mat-icon>exit_to_app</mat-icon>
          <span>Logout</span>
        </button>
      </mat-menu>
    </mat-toolbar>
  `,
  styles: [`
    .blossom-topbar {
      height: 56px;
      background: white;
      border-bottom: 1px solid #e7eaec;
      color: #676a6c;
      box-shadow: none;
    }
    .spacer { flex: 1; }
    .user-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      width: auto;
      border-radius: 4px;
    }
    .user-name {
      font-size: 13px;
      font-weight: 400;
    }
    .user-info {
      padding: 12px 16px;
      display: flex;
      flex-direction: column;
      border-bottom: 1px solid #e7eaec;
    }
    .user-info strong { font-size: 14px; color: #676a6c; }
    .user-info small { font-size: 12px; color: #999; margin-top: 2px; }
  `]
})
export class TopbarComponent {
  private readonly authService = inject(AuthService);
  private readonly configService = inject(ConfigurationService);
  private readonly router = inject(Router);

  readonly user = computed(() => this.configService.config()?.user ?? null);

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.router.navigate(['/login']);
    });
  }
}
