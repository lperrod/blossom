import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { RouterModule } from '@angular/router';

export interface Breadcrumb {
  label: string;
  link?: string;
}

@Component({
  selector: 'blossom-breadcrumb',
  standalone: true,
  imports: [RouterModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <nav class="breadcrumb">
      @for (item of items(); track item.label; let last = $last) {
        <a [routerLink]="item.link"
           [class.active]="last">
          {{ item.label }}
          @if (!last) {
            <span class="separator">/</span>
          }
        </a>
      }
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
  items = input<Breadcrumb[]>([]);
}
