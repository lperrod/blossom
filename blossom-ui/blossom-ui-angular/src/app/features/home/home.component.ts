import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ConfigurationService, MenuItem } from '@blossom/core';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, MatCardModule, MatIconModule],
  template: `
    <h2>Welcome to Blossom</h2>
    <div class="home-grid">
      <mat-card *ngFor="let item of topMenuItems" class="home-card" [routerLink]="item.link">
        <mat-card-header>
          <mat-icon mat-card-avatar>{{ getMaterialIcon(item.icon) }}</mat-icon>
          <mat-card-title>{{ item.label }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <p *ngIf="item.items">{{ item.items.length }} items</p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .home-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 16px; }
    .home-card { cursor: pointer; transition: box-shadow 0.2s; }
    .home-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
  `]
})
export class HomeComponent implements OnInit {
  topMenuItems: MenuItem[] = [];

  constructor(private configService: ConfigurationService) {}

  ngOnInit(): void {
    const config = this.configService.config;
    if (config) {
      this.topMenuItems = config.menu;
    }
  }

  getMaterialIcon(faIcon: string): string {
    const map: Record<string, string> = {
      'fa fa-bar-chart': 'bar_chart', 'fa fa-magnet': 'memory', 'fa fa-calendar': 'schedule',
      'fa fa-pencil': 'edit', 'fa fa-plug': 'power', 'fa fa-flask': 'science',
      'fa fa-sitemap': 'account_tree', 'fa fa-user': 'person', 'fa fa-users': 'group',
      'fa fa-key': 'vpn_key', 'fa fa-file': 'description', 'fa fa-folder-open': 'folder_open',
      'fa fa-search': 'search', 'fa fa-star': 'star', 'fa fa-home': 'home',
    };
    return map[faIcon] || 'widgets';
  }
}
