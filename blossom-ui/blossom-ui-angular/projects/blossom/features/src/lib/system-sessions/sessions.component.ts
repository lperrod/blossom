import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { NotificationService } from '@blossom/core';
import { SessionsService, SessionInfo } from './sessions.service';

@Component({
  selector: 'app-sessions',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatChipsModule],
  template: `
    <h2>Active Sessions</h2>
    <table mat-table [dataSource]="sessions" class="full-width">
      <ng-container matColumnDef="principal">
        <th mat-header-cell *matHeaderCellDef>User</th>
        <td mat-cell *matCellDef="let s">{{ s.principal }}</td>
      </ng-container>
      <ng-container matColumnDef="sessionId">
        <th mat-header-cell *matHeaderCellDef>Session ID</th>
        <td mat-cell *matCellDef="let s">{{ s.sessionId }}</td>
      </ng-container>
      <ng-container matColumnDef="lastRequest">
        <th mat-header-cell *matHeaderCellDef>Last Request</th>
        <td mat-cell *matCellDef="let s">{{ s.lastRequest | date:'medium' }}</td>
      </ng-container>
      <ng-container matColumnDef="expired">
        <th mat-header-cell *matHeaderCellDef>Status</th>
        <td mat-cell *matCellDef="let s">
          <mat-chip [color]="s.expired ? 'warn' : 'primary'" selected>{{ s.expired ? 'Expired' : 'Active' }}</mat-chip>
        </td>
      </ng-container>
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let s">
          <button mat-icon-button color="warn" (click)="invalidate(s.sessionId)" *ngIf="!s.expired">
            <mat-icon>block</mat-icon>
          </button>
        </td>
      </ng-container>
      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
    </table>
  `,
  styles: [`.full-width { width: 100%; }`]
})
export class SessionsComponent implements OnInit {
  sessions: SessionInfo[] = [];
  displayedColumns = ['principal', 'sessionId', 'lastRequest', 'expired', 'actions'];

  constructor(private sessionsService: SessionsService, private notify: NotificationService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.sessionsService.list().subscribe(data => this.sessions = data.sessions);
  }

  invalidate(sessionId: string): void {
    this.sessionsService.invalidate(sessionId).subscribe(() => {
      this.notify.success('Session invalidated');
      this.load();
    });
  }
}
