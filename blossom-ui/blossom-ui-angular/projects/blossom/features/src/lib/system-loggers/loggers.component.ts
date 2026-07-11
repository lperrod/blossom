import { Component, OnInit, ChangeDetectionStrategy, inject } from '@angular/core';
import { MatTreeModule, MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FlatTreeControl } from '@angular/cdk/tree';
import { FormsModule } from '@angular/forms';
import { SearchBarComponent } from '@blossom/ui';
import { NotificationService } from '@blossom/core';
import { LoggersService } from './loggers.service';

interface LoggerNode { id: string; text: string; data?: string; children?: LoggerNode[]; }
interface FlatLoggerNode { expandable: boolean; id: string; text: string; data?: string; level: number; }

@Component({
  selector: 'app-loggers',
  standalone: true,
  imports: [MatTreeModule, MatButtonModule, MatIconModule, MatSelectModule, MatFormFieldModule, FormsModule, SearchBarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2>Loggers</h2>
    <blossom-search-bar placeholder="Filter loggers..." (search)="onSearch($event)"></blossom-search-bar>
    <mat-tree [dataSource]="dataSource" [treeControl]="treeControl">
      <mat-tree-node *matTreeNodeDef="let node" matTreeNodePadding>
        <button mat-icon-button disabled></button>
        <span class="logger-name">{{ node.text }}</span>
        <mat-form-field appearance="outline" class="level-select">
          <mat-select [value]="node.data || ''" (selectionChange)="setLevel(node.id, $event.value)">
            <mat-option value="">INHERITED</mat-option>
            @for (level of levels; track level) {
              <mat-option [value]="level">{{ level }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
      </mat-tree-node>
      <mat-tree-node *matTreeNodeDef="let node; when: hasChild" matTreeNodePadding>
        <button mat-icon-button matTreeNodeToggle>
          <mat-icon>{{ treeControl.isExpanded(node) ? 'expand_more' : 'chevron_right' }}</mat-icon>
        </button>
        <span class="logger-name">{{ node.text }}</span>
        <mat-form-field appearance="outline" class="level-select">
          <mat-select [value]="node.data || ''" (selectionChange)="setLevel(node.id, $event.value)">
            <mat-option value="">INHERITED</mat-option>
            @for (level of levels; track level) {
              <mat-option [value]="level">{{ level }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
      </mat-tree-node>
    </mat-tree>
  `,
  styles: [`
    .logger-name { flex: 1; margin-right: 16px; font-family: monospace; font-size: 13px; }
    .level-select { width: 150px; font-size: 12px; }
    ::ng-deep .level-select .mat-mdc-form-field-infix { padding-top: 4px !important; padding-bottom: 4px !important; min-height: 32px !important; }
  `]
})
export class LoggersComponent implements OnInit {
  levels = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'OFF'];

  private transformer = (node: LoggerNode, level: number): FlatLoggerNode => ({
    expandable: !!node.children && node.children.length > 0,
    id: node.id, text: node.text, data: node.data, level
  });

  treeControl = new FlatTreeControl<FlatLoggerNode>(n => n.level, n => n.expandable);
  treeFlattener = new MatTreeFlattener(this.transformer, n => n.level, n => n.expandable, n => n.children);
  dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  hasChild = (_: number, node: FlatLoggerNode) => node.expandable;

  private loggersService = inject(LoggersService);
  private notify = inject(NotificationService);

  ngOnInit(): void { this.load(); }

  load(q = ''): void {
    this.loggersService.tree(q).subscribe(data => {
      this.dataSource.data = data.children || [];
    });
  }

  onSearch(q: string): void { this.load(q); }

  setLevel(name: string, level: string): void {
    if (level) {
      this.loggersService.setLevel(name, level).subscribe(() => this.notify.success(`Logger ${name} set to ${level}`));
    }
  }
}
