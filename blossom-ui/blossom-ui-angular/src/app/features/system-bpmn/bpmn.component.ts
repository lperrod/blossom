import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { BpmnService, ProcessDefinition } from './bpmn.service';

@Component({
  selector: 'app-bpmn',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatChipsModule],
  template: `
    <h2>BPMN Process Definitions</h2>
    <table mat-table [dataSource]="definitions" class="full-width">
      <ng-container matColumnDef="key">
        <th mat-header-cell *matHeaderCellDef>Key</th>
        <td mat-cell *matCellDef="let d">{{ d.key }}</td>
      </ng-container>
      <ng-container matColumnDef="name">
        <th mat-header-cell *matHeaderCellDef>Name</th>
        <td mat-cell *matCellDef="let d">{{ d.name }}</td>
      </ng-container>
      <ng-container matColumnDef="version">
        <th mat-header-cell *matHeaderCellDef>Version</th>
        <td mat-cell *matCellDef="let d">{{ d.version }}</td>
      </ng-container>
      <ng-container matColumnDef="runningInstances">
        <th mat-header-cell *matHeaderCellDef>Running Instances</th>
        <td mat-cell *matCellDef="let d">
          <mat-chip [color]="d.runningInstances > 0 ? 'primary' : 'accent'" selected>{{ d.runningInstances }}</mat-chip>
        </td>
      </ng-container>
      <ng-container matColumnDef="resourceName">
        <th mat-header-cell *matHeaderCellDef>Resource</th>
        <td mat-cell *matCellDef="let d">{{ d.resourceName }}</td>
      </ng-container>
      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns;"></tr>
    </table>
  `,
  styles: [`.full-width { width: 100%; }`]
})
export class BpmnComponent implements OnInit {
  definitions: ProcessDefinition[] = [];
  columns = ['key', 'name', 'version', 'runningInstances', 'resourceName'];

  constructor(private bpmnService: BpmnService) {}

  ngOnInit(): void {
    this.bpmnService.get().subscribe(data => this.definitions = data.definitions);
  }
}
