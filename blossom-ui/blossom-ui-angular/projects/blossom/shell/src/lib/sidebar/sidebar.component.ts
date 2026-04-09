import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MenuService, MenuItem } from '@blossom/core';

@Component({
  selector: 'blossom-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatListModule, MatIconModule, MatExpansionModule],
  template: `
    <div class="sidebar-header">
      <div class="logo-area">
        <h3 class="brand">Blossom</h3>
      </div>
    </div>
    <mat-nav-list class="sidebar-nav">
      <ng-container *ngFor="let item of menuItems">
        <!-- Leaf items (no children) -->
        <ng-container *ngIf="!item.items || item.items.length === 0">
          <a mat-list-item [routerLink]="item.link" routerLinkActive="active"
             [routerLinkActiveOptions]="item.link === '/' ? {exact: true} : {exact: false}">
            <i class="{{ item.icon }}" matListItemIcon></i>
            <span matListItemTitle>{{ item.label }}</span>
          </a>
        </ng-container>
        <!-- Parent items (with children) -->
        <ng-container *ngIf="item.items && item.items.length > 0">
          <mat-expansion-panel class="menu-group" [expanded]="false">
            <mat-expansion-panel-header>
              <mat-panel-title>
                <i class="{{ item.icon }}"></i>
                <span>{{ item.label }}</span>
              </mat-panel-title>
            </mat-expansion-panel-header>
            <mat-nav-list dense>
              <a mat-list-item *ngFor="let child of item.items"
                 [routerLink]="child.link" routerLinkActive="active">
                <i class="{{ child.icon }}" matListItemIcon></i>
                <span matListItemTitle>{{ child.label }}</span>
              </a>
            </mat-nav-list>
          </mat-expansion-panel>
        </ng-container>
      </ng-container>
    </mat-nav-list>
  `,
  styles: [`
    :host {
      display: block;
      height: 100%;
      color: #a7b1c2;
      overflow-y: auto;
    }
    .sidebar-header {
      padding: 20px 16px;
      background: #293846;
      text-align: center;
      border-bottom: 1px solid rgba(255,255,255,0.05);
    }
    .brand {
      color: white;
      margin: 0;
      font-size: 22px;
      font-weight: 300;
      letter-spacing: 1px;
    }
    .sidebar-nav {
      padding-top: 8px;
    }

    /* Menu group (expansion panel) */
    .menu-group {
      background: transparent !important;
      box-shadow: none !important;
      color: #a7b1c2;
      border-radius: 0 !important;
    }
    ::ng-deep .menu-group .mat-expansion-panel-body { padding: 0 !important; }
    ::ng-deep .menu-group .mat-expansion-panel-header {
      padding: 0 16px !important;
      height: 44px !important;
      color: #a7b1c2 !important;
      font-size: 14px;
    }
    ::ng-deep .menu-group .mat-expansion-panel-header:hover {
      background: rgba(255,255,255,0.05) !important;
    }
    ::ng-deep .menu-group .mat-expansion-indicator::after {
      color: #a7b1c2 !important;
    }
    ::ng-deep .menu-group .mat-expansion-panel-header .mat-content {
      align-items: center;
    }
    ::ng-deep .menu-group mat-panel-title {
      color: #a7b1c2 !important;
      font-weight: 400;
      align-items: center;
      display: flex;
    }

    /* Nav items */
    mat-nav-list a {
      color: #a7b1c2 !important;
      font-size: 13px !important;
    }
    mat-nav-list a:hover {
      color: white !important;
      background: rgba(255,255,255,0.05) !important;
    }
    .active {
      color: white !important;
      background: #293846 !important;
      border-left: 3px solid #1ab394 !important;
    }

    /* Icons */
    i {
      margin-right: 10px;
      width: 18px;
      text-align: center;
      font-size: 14px;
    }
  `]
})
export class SidebarComponent implements OnInit {
  menuItems: MenuItem[] = [];

  constructor(private menuService: MenuService) {}

  ngOnInit(): void {
    this.menuService.menu$.subscribe(menu => {
      this.menuItems = menu;
    });
  }
}
