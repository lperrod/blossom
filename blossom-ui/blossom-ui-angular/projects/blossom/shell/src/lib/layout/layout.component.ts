import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';

@Component({
  selector: 'blossom-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, MatSidenavModule, MatToolbarModule, SidebarComponent, TopbarComponent],
  template: `
    <mat-sidenav-container class="blossom-layout">
      <mat-sidenav mode="side" opened class="blossom-sidebar">
        <blossom-sidebar></blossom-sidebar>
      </mat-sidenav>
      <mat-sidenav-content class="blossom-content">
        <blossom-topbar></blossom-topbar>
        <main class="blossom-main">
          <router-outlet></router-outlet>
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .blossom-layout { height: 100vh; }
    .blossom-sidebar { width: 260px; background: #2f4050; }
    .blossom-content { display: flex; flex-direction: column; }
    .blossom-main { flex: 1; padding: 24px; overflow-y: auto; background: #f3f3f4; }
  `]
})
export class LayoutComponent {}
