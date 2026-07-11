import { Component, OnInit, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { ConfigurationService, MenuItem } from '@blossom/core';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterModule, MatCardModule, MatIconModule, MatListModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2>Welcome to Blossom</h2>
    <div class="home-grid">
      @for (item of topMenuItems(); track item.label) {
        <mat-card class="home-card" (click)="navigate(item)">
          <mat-card-header>
            <mat-icon mat-card-avatar class="card-icon">{{ getMaterialIcon(item.icon) }}</mat-icon>
            <mat-card-title>{{ item.label }}</mat-card-title>
          </mat-card-header>
          @if (item.items && item.items.length > 0) {
            <mat-card-content>
              <mat-nav-list dense>
                @for (child of item.items; track child.label) {
                  <a mat-list-item [routerLink]="child.link">
                    <i class="{{ child.icon }}" matListItemIcon></i>
                    <span matListItemTitle>{{ child.label }}</span>
                  </a>
                }
              </mat-nav-list>
            </mat-card-content>
          }
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .home-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }
    .home-card { cursor: pointer; transition: box-shadow 0.2s; }
    .home-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
    .card-icon { color: #1ab394; font-size: 32px; width: 32px; height: 32px; }
    mat-card-title { color: #676a6c; }
    mat-nav-list a { font-size: 13px !important; }
    i { margin-right: 8px; width: 16px; text-align: center; color: #999; }
  `]
})
export class HomeComponent implements OnInit {
  topMenuItems = signal<MenuItem[]>([]);

  private configService = inject(ConfigurationService);
  private router = inject(Router);

  ngOnInit(): void {
    const config = this.configService.config();
    if (config) {
      this.topMenuItems.set(config.menu);
    }
  }

  navigate(item: MenuItem): void {
    if (item.items && item.items.length > 0) {
      this.router.navigate([item.items[0].link]);
    } else if (item.link) {
      this.router.navigate([item.link]);
    }
  }

  getMaterialIcon(faIcon: string): string {
    if (!faIcon) return 'widgets';
    const map: Record<string, string> = {
      'fa fa-bar-chart': 'bar_chart', 'fa fa-magnet': 'memory', 'fa fa-calendar': 'schedule',
      'fa fa-pencil': 'edit', 'fa fa-plug': 'power', 'fa fa-flask': 'science',
      'fa fa-sitemap': 'account_tree', 'fa fa-user': 'person', 'fa fa-users': 'group',
      'fa fa-key': 'vpn_key', 'fa fa-file': 'description', 'fa fa-folder-open': 'folder_open',
      'fa fa-search': 'search', 'fa fa-star': 'star', 'fa fa-home': 'home',
      'glyphicon glyphicon-list-alt': 'admin_panel_settings', 'fa fa-cogs': 'settings',
    };
    return map[faIcon] || 'widgets';
  }
}
