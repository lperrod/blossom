import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

export interface Breadcrumb {
  label: string;
  link?: string;
}

@Component({
  selector: 'blossom-breadcrumb',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="breadcrumb">
      <a *ngFor="let item of items; let last = last"
         [routerLink]="item.link"
         [class.active]="last">
        {{ item.label }}
        <span *ngIf="!last" class="separator">/</span>
      </a>
    </nav>
  `,
  styles: [`
    .breadcrumb { padding: 8px 0; font-size: 14px; }
    .breadcrumb a { color: #666; text-decoration: none; }
    .breadcrumb a.active { color: #333; font-weight: 500; }
    .separator { margin: 0 8px; }
  `]
})
export class BreadcrumbComponent {
  @Input() items: Breadcrumb[] = [];
}
