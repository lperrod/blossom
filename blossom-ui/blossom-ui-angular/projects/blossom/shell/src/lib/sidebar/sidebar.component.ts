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
      <h3 class="brand">Blossom</h3>
    </div>
    <mat-nav-list>
      <ng-container *ngFor="let item of menuItems">
        <ng-container *ngIf="!item.items || item.items.length === 0">
          <a mat-list-item [routerLink]="item.link" routerLinkActive="active">
            <i class="{{ item.icon }}" matListItemIcon></i>
            <span matListItemTitle>{{ item.label }}</span>
          </a>
        </ng-container>
        <ng-container *ngIf="item.items && item.items.length > 0">
          <mat-expansion-panel class="menu-group" [expanded]="false">
            <mat-expansion-panel-header>
              <mat-panel-title>
                <i class="{{ item.icon }}"></i>
                <span>{{ item.label }}</span>
              </mat-panel-title>
            </mat-expansion-panel-header>
            <mat-nav-list>
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
    :host { display: block; height: 100%; color: #a7b1c2; }
    .sidebar-header { padding: 20px; text-align: center; }
    .brand { color: white; margin: 0; font-size: 20px; }
    .menu-group { background: transparent; box-shadow: none; color: #a7b1c2; }
    ::ng-deep .menu-group .mat-expansion-panel-body { padding: 0; }
    .active { background: rgba(255,255,255,0.1) !important; }
    mat-nav-list a { color: #a7b1c2; }
    mat-nav-list a:hover { color: white; background: rgba(255,255,255,0.05); }
    i { margin-right: 10px; width: 20px; text-align: center; }
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
