import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { DashboardService } from './dashboard.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatProgressBarModule, MatChipsModule],
  template: `
    <h2>System Dashboard</h2>
    <div class="dashboard-grid">
      <mat-card>
        <mat-card-header><mat-card-title>Health Status</mat-card-title></mat-card-header>
        <mat-card-content>
          <div class="status-info" *ngIf="status">
            <mat-chip [color]="status.health?.status === 'UP' ? 'primary' : 'warn'" selected>
              {{ status.health?.status || 'UNKNOWN' }}
            </mat-chip>
            <p>Uptime: {{ formatUptime(status.uptime) }}</p>
          </div>
        </mat-card-content>
      </mat-card>

      <mat-card>
        <mat-card-header><mat-card-title>Memory</mat-card-title></mat-card-header>
        <mat-card-content *ngIf="memory">
          <div class="metric">
            <span>Heap Used</span>
            <mat-progress-bar mode="determinate" [value]="heapPercent"></mat-progress-bar>
            <span>{{ formatBytes(memory.heap_used) }} / {{ formatBytes(memory.heap_max) }}</span>
          </div>
          <div class="metric">
            <span>Total Used</span>
            <span>{{ formatBytes(memory.total_used) }} / {{ formatBytes(memory.total_max) }}</span>
          </div>
        </mat-card-content>
      </mat-card>

      <mat-card>
        <mat-card-header><mat-card-title>JVM</mat-card-title></mat-card-header>
        <mat-card-content *ngIf="jvm">
          <div class="metric-row">
            <div class="metric-item"><strong>{{ jvm.classes_loaded | number }}</strong><span>Classes Loaded</span></div>
            <div class="metric-item"><strong>{{ jvm.threads_live | number }}</strong><span>Live Threads</span></div>
            <div class="metric-item"><strong>{{ jvm.threads_daemon | number }}</strong><span>Daemon Threads</span></div>
            <div class="metric-item"><strong>{{ jvm.processors }}</strong><span>Processors</span></div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .dashboard-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(400px, 1fr)); gap: 16px; }
    .status-info { display: flex; align-items: center; gap: 16px; }
    .metric { margin: 12px 0; }
    .metric span { display: block; font-size: 13px; color: #666; }
    .metric mat-progress-bar { margin: 4px 0; }
    .metric-row { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
    .metric-item { text-align: center; }
    .metric-item strong { display: block; font-size: 24px; }
    .metric-item span { font-size: 12px; color: #666; }
  `]
})
export class DashboardComponent implements OnInit, OnDestroy {
  status: any;
  memory: any;
  jvm: any;
  heapPercent = 0;
  private pollSub?: Subscription;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadAll();
    this.pollSub = interval(10000).subscribe(() => this.loadAll());
  }

  ngOnDestroy(): void { this.pollSub?.unsubscribe(); }

  loadAll(): void {
    this.dashboardService.getStatus().subscribe(d => this.status = d);
    this.dashboardService.getMemory().subscribe(d => {
      this.memory = d;
      this.heapPercent = d.heap_max > 0 ? (d.heap_used / d.heap_max) * 100 : 0;
    });
    this.dashboardService.getJvm().subscribe(d => this.jvm = d);
  }

  formatUptime(ms: number): string {
    if (!ms) return '';
    const s = Math.floor(ms / 1000); const m = Math.floor(s / 60); const h = Math.floor(m / 60); const d = Math.floor(h / 24);
    return `${d}d ${h % 24}h ${m % 60}m`;
  }

  formatBytes(bytes: number): string {
    if (!bytes) return '0 B';
    const k = 1024; const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }
}
