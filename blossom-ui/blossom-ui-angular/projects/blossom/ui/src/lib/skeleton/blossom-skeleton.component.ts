import { Component, ChangeDetectionStrategy, computed, input } from '@angular/core';

/**
 * Grey shimmering placeholder shown while content loads: `lines` bars of `height`, the last one shorter when there
 * are several, like a paragraph. Hidden from screen readers, the parent tells what is loading.
 */
@Component({
  selector: 'blossom-skeleton',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (line of lineIndexes(); track line) {
      <span class="blossom-skeleton-bar" [style.height]="height()"
            [style.width]="line === lineIndexes().length - 1 && lineIndexes().length > 1 ? '60%' : width()"></span>
    }
  `,
  host: { 'aria-hidden': 'true', class: 'blossom-skeleton' },
  styles: [`
    :host { display: block; }
    .blossom-skeleton-bar {
      display: block;
      border-radius: 4px;
      background: linear-gradient(90deg, rgba(127, 127, 127, 0.12) 25%, rgba(127, 127, 127, 0.24) 37%, rgba(127, 127, 127, 0.12) 63%);
      background-size: 400% 100%;
      animation: blossom-skeleton-shimmer 1.4s ease infinite;
    }
    .blossom-skeleton-bar + .blossom-skeleton-bar { margin-top: 8px; }
    @keyframes blossom-skeleton-shimmer {
      0% { background-position: 100% 50%; }
      100% { background-position: 0 50%; }
    }
    @media (prefers-reduced-motion: reduce) {
      .blossom-skeleton-bar { animation: none; }
    }
  `]
})
export class BlossomSkeletonComponent {
  lines = input(1);
  height = input('16px');
  width = input('100%');

  lineIndexes = computed(() => Array.from({ length: Math.max(1, this.lines()) }, (_, i) => i));
}
