import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatToolbarModule } from '@angular/material/toolbar';
import { SearchBarComponent } from '@blossom/ui';
import { NotificationService } from '@blossom/core';
import { CachesService, CacheInfo } from './caches.service';

@Component({
  selector: 'app-caches',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatSlideToggleModule, MatToolbarModule, SearchBarComponent],
  template: `
    <div class="page-header">
      <h2>Caches</h2>
      <button mat-flat-button color="warn" (click)="emptyAll()">
        <mat-icon>delete_sweep</mat-icon> Empty All
      </button>
    </div>
    <blossom-search-bar placeholder="Filter caches..." (search)="onSearch($event)"></blossom-search-bar>
    <table mat-table [dataSource]="caches" class="full-width">
      <ng-container matColumnDef="name">
        <th mat-header-cell *matHeaderCellDef>Name</th>
        <td mat-cell *matCellDef="let c">{{ c.name }}</td>
      </ng-container>
      <ng-container matColumnDef="enabled">
        <th mat-header-cell *matHeaderCellDef>Enabled</th>
        <td mat-cell *matCellDef="let c">
          <mat-slide-toggle [checked]="c.enabled" (change)="toggleCache(c)"></mat-slide-toggle>
        </td>
      </ng-container>
      <ng-container matColumnDef="size">
        <th mat-header-cell *matHeaderCellDef>Size</th>
        <td mat-cell *matCellDef="let c">{{ c.size }}</td>
      </ng-container>
      <ng-container matColumnDef="hits">
        <th mat-header-cell *matHeaderCellDef>Hits</th>
        <td mat-cell *matCellDef="let c">{{ c.hits }}</td>
      </ng-container>
      <ng-container matColumnDef="misses">
        <th mat-header-cell *matHeaderCellDef>Misses</th>
        <td mat-cell *matCellDef="let c">{{ c.misses }}</td>
      </ng-container>
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let c">
          <button mat-icon-button color="warn" (click)="emptyCache(c.name)" matTooltip="Empty cache">
            <mat-icon>delete</mat-icon>
          </button>
        </td>
      </ng-container>
      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
    </table>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .full-width { width: 100%; }
  `]
})
export class CachesComponent implements OnInit {
  caches: CacheInfo[] = [];
  displayedColumns = ['name', 'enabled', 'size', 'hits', 'misses', 'actions'];
  private query = '';

  constructor(private cachesService: CachesService, private notify: NotificationService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.cachesService.list(this.query).subscribe(data => this.caches = data);
  }

  onSearch(q: string): void { this.query = q; this.load(); }

  emptyCache(name: string): void {
    this.cachesService.emptyCache(name).subscribe(() => { this.notify.success('Cache emptied'); this.load(); });
  }

  toggleCache(cache: CacheInfo): void {
    const obs = cache.enabled ? this.cachesService.disableCache(cache.name) : this.cachesService.enableCache(cache.name);
    obs.subscribe(() => { this.notify.success(cache.enabled ? 'Cache disabled' : 'Cache enabled'); this.load(); });
  }

  emptyAll(): void {
    this.cachesService.emptyAll().subscribe(() => { this.notify.success('All caches emptied'); this.load(); });
  }
}
