import { Component, ChangeDetectionStrategy, ViewEncapsulation, inject, viewChild } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { BreakpointObserver } from '@angular/cdk/layout';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { filter, map } from 'rxjs';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';

/** Below this width the menu floats over the content and closes after each navigation. */
export const BLOSSOM_HANDSET_QUERY = '(max-width: 959.98px)';

@Component({
  selector: 'blossom-layout',
  standalone: true,
  imports: [RouterModule, MatSidenavModule, MatToolbarModule, SidebarComponent, TopbarComponent],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-sidenav-container class="blossom-layout" [class.blossom-handset]="isHandset()">
      <mat-sidenav #sidenav class="blossom-sidebar"
                   [mode]="isHandset() ? 'over' : 'side'"
                   [opened]="!isHandset()"
                   [fixedInViewport]="isHandset()">
        <blossom-sidebar (navigate)="closeOnHandset()"></blossom-sidebar>
      </mat-sidenav>
      <mat-sidenav-content class="blossom-content">
        <blossom-topbar (menuToggle)="sidenav.toggle()"></blossom-topbar>
        <main class="blossom-main">
          <router-outlet></router-outlet>
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .blossom-layout {
      height: 100vh;
      height: 100dvh;
    }
    .blossom-layout .mat-drawer.blossom-sidebar,
    .blossom-sidebar {
      width: 260px;
      max-width: 85vw;
      background-color: #ffffff !important;
      border-right: 1px solid #e7eaec;
      color: #676a6c;
    }
    .blossom-content {
      display: flex;
      flex-direction: column;
    }
    .blossom-main {
      flex: 1;
      padding: 24px;
      overflow-y: auto;
      background: #f3f3f4;
      min-width: 0;
    }
    /* Shared helper: wrap wide tables so they scroll instead of overflowing the page */
    .table-scroll {
      width: 100%;
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }
    @media (max-width: 599.98px) {
      .blossom-main {
        padding: 12px;
      }
    }
  `]
})
export class LayoutComponent {
  private readonly sidenav = viewChild.required<MatSidenav>('sidenav');

  readonly isHandset = toSignal(
    inject(BreakpointObserver).observe(BLOSSOM_HANDSET_QUERY).pipe(map(state => state.matches)),
    { initialValue: false }
  );

  constructor() {
    inject(Router).events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntilDestroyed()
    ).subscribe(() => this.closeOnHandset());
  }

  closeOnHandset(): void {
    if (this.isHandset()) {
      this.sidenav().close();
    }
  }
}
