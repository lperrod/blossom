import { Component, ChangeDetectionStrategy, ViewEncapsulation, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { toSignal } from '@angular/core/rxjs-interop';
import { MenuService, BLOSSOM_APP_CONFIG } from '@blossom/core';

@Component({
  selector: 'blossom-sidebar',
  standalone: true,
  imports: [RouterModule, MatListModule, MatIconModule, MatExpansionModule],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="sidebar-header">
      <div class="logo-area">
        <h3 class="brand">{{ brandName }}</h3>
      </div>
    </div>
    <mat-nav-list class="sidebar-nav">
      @for (item of menuItems(); track item.link) {
        <!-- Leaf items (no children) -->
        @if (!item.items || item.items.length === 0) {
          <a mat-list-item [routerLink]="item.link" routerLinkActive="active"
             [routerLinkActiveOptions]="item.link === '/' ? {exact: true} : {exact: false}">
            <i class="{{ item.icon }}" matListItemIcon></i>
            <span matListItemTitle>{{ item.label }}</span>
          </a>
        }
        <!-- Parent items (with children) -->
        @if (item.items && item.items.length > 0) {
          <mat-expansion-panel class="menu-group" [expanded]="false">
            <mat-expansion-panel-header>
              <mat-panel-title>
                <i class="{{ item.icon }}"></i>
                <span>{{ item.label }}</span>
              </mat-panel-title>
            </mat-expansion-panel-header>
            <mat-nav-list dense>
              @for (child of item.items; track child.link) {
                <a mat-list-item
                   [routerLink]="child.link" routerLinkActive="active">
                  <i class="{{ child.icon }}" matListItemIcon></i>
                  <span matListItemTitle>{{ child.label }}</span>
                </a>
              }
            </mat-nav-list>
          </mat-expansion-panel>
        }
      }
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
      background: #1ab394;
      text-align: center;
      border-bottom: 1px solid #e7eaec;
    }
    .brand {
      color: white;
      margin: 0;
      font-size: 22px;
      font-weight: 400;
      letter-spacing: 1px;
    }
    .sidebar-nav {
      padding-top: 8px;
    }

    /* Top-level nav items */
    .sidebar-nav > a.mat-mdc-list-item {
      color: #676a6c !important;
      font-size: 14px !important;
      height: 44px !important;
      border-bottom: 1px solid #e7eaec;
    }
    .sidebar-nav > a.mat-mdc-list-item:hover {
      color: #333 !important;
      background: #f3f3f4 !important;
    }

    /* Menu group (expansion panel) */
    .menu-group {
      background: transparent !important;
      box-shadow: none !important;
      color: #676a6c;
      border-radius: 0 !important;
      border-bottom: 1px solid #e7eaec;
    }
    .menu-group .mat-expansion-panel-body { padding: 0 !important; background: #f8f8f9; }
    .menu-group .mat-expansion-panel-header {
      padding: 0 16px !important;
      height: 44px !important;
      color: #676a6c !important;
      font-size: 14px;
    }
    .menu-group .mat-expansion-panel-header:hover {
      background: #f3f3f4 !important;
    }
    .menu-group .mat-expansion-indicator::after {
      color: #999 !important;
    }
    .menu-group .mat-expansion-panel-header .mat-content {
      align-items: center;
    }
    .menu-group mat-panel-title {
      color: #676a6c !important;
      font-weight: 500;
      align-items: center;
      display: flex;
      font-size: 14px;
    }

    /* Child nav items (inside expansion panel) */
    .menu-group mat-nav-list a.mat-mdc-list-item {
      color: #676a6c !important;
      font-size: 13px !important;
      height: 38px !important;
      padding-left: 32px !important;
    }
    .menu-group mat-nav-list a.mat-mdc-list-item:hover {
      color: #333 !important;
      background: #eee !important;
    }

    /* Active state */
    a.active {
      color: #1ab394 !important;
      background: #f0faf7 !important;
      border-left: 3px solid #1ab394 !important;
      font-weight: 500;
    }

    /* Icons */
    i {
      margin-right: 10px;
      width: 18px;
      text-align: center;
      font-size: 14px;
      color: #999;
    }
    a.active i {
      color: #1ab394 !important;
    }
  `]
})
export class SidebarComponent {
  private readonly appConfig = inject(BLOSSOM_APP_CONFIG);
  private readonly menuService = inject(MenuService);

  readonly brandName = this.appConfig.appName;
  readonly menuItems = toSignal(this.menuService.menu$, { initialValue: [] });
}
