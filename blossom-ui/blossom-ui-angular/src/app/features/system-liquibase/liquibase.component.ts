import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { LiquibaseService } from './liquibase.service';

@Component({
  selector: 'app-liquibase',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatExpansionModule, MatChipsModule],
  template: `
    <h2>Liquibase Migrations</h2>
    <mat-accordion *ngFor="let report of reports | keyvalue">
      <mat-expansion-panel [expanded]="true">
        <mat-expansion-panel-header>
          <mat-panel-title>{{ report.key }}</mat-panel-title>
        </mat-expansion-panel-header>
        <table mat-table [dataSource]="report.value" class="full-width">
          <ng-container matColumnDef="id">
            <th mat-header-cell *matHeaderCellDef>ID</th>
            <td mat-cell *matCellDef="let cs">{{ cs.id }}</td>
          </ng-container>
          <ng-container matColumnDef="author">
            <th mat-header-cell *matHeaderCellDef>Author</th>
            <td mat-cell *matCellDef="let cs">{{ cs.author }}</td>
          </ng-container>
          <ng-container matColumnDef="description">
            <th mat-header-cell *matHeaderCellDef>Description</th>
            <td mat-cell *matCellDef="let cs">{{ cs.description }}</td>
          </ng-container>
          <ng-container matColumnDef="dateExecuted">
            <th mat-header-cell *matHeaderCellDef>Date Executed</th>
            <td mat-cell *matCellDef="let cs">{{ cs.dateExecuted | date:'medium' }}</td>
          </ng-container>
          <ng-container matColumnDef="execType">
            <th mat-header-cell *matHeaderCellDef>Type</th>
            <td mat-cell *matCellDef="let cs">
              <mat-chip>{{ cs.execType }}</mat-chip>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns;"></tr>
        </table>
      </mat-expansion-panel>
    </mat-accordion>
  `,
  styles: [`.full-width { width: 100%; }`]
})
export class LiquibaseComponent implements OnInit {
  reports: Record<string, any[]> = {};
  columns = ['id', 'author', 'description', 'dateExecuted', 'execType'];

  constructor(private liquibaseService: LiquibaseService) {}

  ngOnInit(): void {
    this.liquibaseService.getReports().subscribe(data => this.reports = data);
  }
}
