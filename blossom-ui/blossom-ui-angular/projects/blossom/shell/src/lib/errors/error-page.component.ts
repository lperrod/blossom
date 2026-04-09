import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'blossom-error-page',
  standalone: true,
  imports: [CommonModule, RouterModule, MatButtonModule, MatIconModule],
  template: `
    <div class="error-page">
      <mat-icon class="error-icon">{{ icon }}</mat-icon>
      <h1>{{ code }}</h1>
      <p>{{ message }}</p>
      <a mat-flat-button color="primary" routerLink="/">Go Home</a>
    </div>
  `,
  styles: [`
    .error-page { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh; text-align: center; }
    .error-icon { font-size: 64px; width: 64px; height: 64px; color: #999; margin-bottom: 16px; }
    h1 { font-size: 48px; color: #333; margin: 0; }
    p { font-size: 18px; color: #666; margin: 16px 0 32px; }
  `]
})
export class ErrorPageComponent {
  @Input() code = '404';
  @Input() message = 'Page not found';
  @Input() icon = 'error_outline';
}
