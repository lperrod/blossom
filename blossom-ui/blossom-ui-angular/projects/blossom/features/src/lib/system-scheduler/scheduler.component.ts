import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatChipsModule } from '@angular/material/chips';
import { NotificationService } from '@blossom/core';
import { SchedulerService, JobInfo } from './scheduler.service';

@Component({
  selector: 'app-scheduler',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatExpansionModule, MatSlideToggleModule, MatChipsModule],
  template: `
    <div class="page-header">
      <h2>Scheduler</h2>
      <mat-slide-toggle [checked]="schedulerActive" (change)="toggleScheduler($event.checked)">
        {{ schedulerActive ? 'Active' : 'Inactive' }}
      </mat-slide-toggle>
    </div>
    <mat-accordion>
      <mat-expansion-panel *ngFor="let group of groups" (opened)="loadGroup(group)">
        <mat-expansion-panel-header>
          <mat-panel-title>{{ group }}</mat-panel-title>
        </mat-expansion-panel-header>
        <table mat-table [dataSource]="jobsByGroup[group] || []" class="full-width">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let j">{{ j.name }}</td>
          </ng-container>
          <ng-container matColumnDef="description">
            <th mat-header-cell *matHeaderCellDef>Description</th>
            <td mat-cell *matCellDef="let j">{{ j.description }}</td>
          </ng-container>
          <ng-container matColumnDef="state">
            <th mat-header-cell *matHeaderCellDef>State</th>
            <td mat-cell *matCellDef="let j"><mat-chip>{{ j.state }}</mat-chip></td>
          </ng-container>
          <ng-container matColumnDef="nextFireTime">
            <th mat-header-cell *matHeaderCellDef>Next Fire</th>
            <td mat-cell *matCellDef="let j">{{ j.nextFireTime | date:'medium' }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let j">
              <button mat-icon-button color="primary" (click)="executeJob(j.group, j.name)">
                <mat-icon>play_arrow</mat-icon>
              </button>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="jobColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: jobColumns;"></tr>
        </table>
      </mat-expansion-panel>
    </mat-accordion>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .full-width { width: 100%; }
  `]
})
export class SchedulerComponent implements OnInit {
  groups: string[] = [];
  jobsByGroup: Record<string, JobInfo[]> = {};
  jobColumns = ['name', 'description', 'state', 'nextFireTime', 'actions'];
  schedulerActive = true;

  constructor(private schedulerService: SchedulerService, private notify: NotificationService) {}

  ngOnInit(): void {
    this.schedulerService.getInfo().subscribe(data => {
      this.groups = data.groups;
      this.schedulerActive = data.info?.started ?? true;
    });
  }

  loadGroup(group: string): void {
    if (!this.jobsByGroup[group]) {
      this.schedulerService.getJobs(group).subscribe(jobs => this.jobsByGroup[group] = jobs);
    }
  }

  executeJob(group: string, name: string): void {
    this.schedulerService.execute(group, name).subscribe(() => this.notify.success(`Job ${name} executed`));
  }

  toggleScheduler(active: boolean): void {
    this.schedulerService.changeState(active).subscribe(() => {
      this.schedulerActive = active;
      this.notify.success(active ? 'Scheduler activated' : 'Scheduler deactivated');
    });
  }
}
