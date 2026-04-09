import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService, ConfigurationService, UserInfo } from '@blossom/core';

@Component({
  selector: 'blossom-topbar',
  standalone: true,
  imports: [CommonModule, MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule],
  template: `
    <mat-toolbar color="primary" class="blossom-topbar">
      <span class="spacer"></span>
      <button mat-icon-button [matMenuTriggerFor]="userMenu" *ngIf="user">
        <mat-icon>account_circle</mat-icon>
      </button>
      <mat-menu #userMenu="matMenu">
        <div class="user-info" *ngIf="user">
          <strong>{{ user.firstname }} {{ user.lastname }}</strong>
          <small>{{ user.email }}</small>
        </div>
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
    .blossom-topbar { height: 56px; }
    .spacer { flex: 1; }
    .user-info { padding: 8px 16px; display: flex; flex-direction: column; border-bottom: 1px solid #eee; }
    .user-info strong { font-size: 14px; }
    .user-info small { font-size: 12px; color: #666; }
  `]
})
export class TopbarComponent implements OnInit {
  user: UserInfo | null = null;

  constructor(
    private authService: AuthService,
    private configService: ConfigurationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const config = this.configService.config;
    if (config) {
      this.user = config.user;
    }
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.router.navigate(['/login']);
    });
  }
}
