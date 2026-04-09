import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTreeModule, MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { FlatTreeControl } from '@angular/cdk/tree';
import { SelectionModel } from '@angular/cdk/collections';

interface PrivilegeNode {
  name: string;
  privilege?: string;
  children?: PrivilegeNode[];
}

interface FlatNode {
  expandable: boolean;
  name: string;
  privilege?: string;
  level: number;
}

@Component({
  selector: 'blossom-privilege-tree',
  standalone: true,
  imports: [CommonModule, MatTreeModule, MatCheckboxModule, MatIconModule, MatButtonModule],
  template: `
    <mat-tree [dataSource]="dataSource" [treeControl]="treeControl">
      <mat-tree-node *matTreeNodeDef="let node" matTreeNodePadding>
        <button mat-icon-button disabled></button>
        <mat-checkbox
          [checked]="checklistSelection.isSelected(node)"
          (change)="toggleLeaf(node)">
          {{ node.name }}
        </mat-checkbox>
      </mat-tree-node>

      <mat-tree-node *matTreeNodeDef="let node; when: hasChild" matTreeNodePadding>
        <button mat-icon-button matTreeNodeToggle>
          <mat-icon>{{ treeControl.isExpanded(node) ? 'expand_more' : 'chevron_right' }}</mat-icon>
        </button>
        <mat-checkbox
          [checked]="descendantsAllSelected(node)"
          [indeterminate]="descendantsPartiallySelected(node)"
          (change)="toggleParent(node)">
          {{ node.name }}
        </mat-checkbox>
      </mat-tree-node>
    </mat-tree>
  `
})
export class PrivilegeTreeComponent {
  @Input() set privileges(value: any[]) {
    this.buildTree(value);
  }
  @Input() set selected(value: string[]) {
    this.setSelected(value);
  }
  @Output() selectionChange = new EventEmitter<string[]>();

  private transformer = (node: PrivilegeNode, level: number): FlatNode => ({
    expandable: !!node.children && node.children.length > 0,
    name: node.name,
    privilege: node.privilege,
    level
  });

  treeControl = new FlatTreeControl<FlatNode>(n => n.level, n => n.expandable);
  treeFlattener = new MatTreeFlattener(this.transformer, n => n.level, n => n.expandable, n => n.children);
  dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  checklistSelection = new SelectionModel<FlatNode>(true);
  hasChild = (_: number, node: FlatNode) => node.expandable;

  private buildTree(privileges: any[]): void {
    if (!privileges) return;
    const nodes: PrivilegeNode[] = privileges.map(ns => ({
      name: ns.namespace,
      children: ns.features?.map((f: any) => ({
        name: f.feature,
        children: f.privileges?.map((p: any) => ({
          name: p.right,
          privilege: p.privilege
        }))
      }))
    }));
    this.dataSource.data = nodes;
    this.treeControl.expandAll();
  }

  private setSelected(values: string[]): void {
    this.checklistSelection.clear();
    const flatNodes = this.treeControl.dataNodes;
    if (flatNodes) {
      flatNodes.filter(n => n.privilege && values.includes(n.privilege))
        .forEach(n => this.checklistSelection.select(n));
    }
  }

  toggleLeaf(node: FlatNode): void {
    this.checklistSelection.toggle(node);
    this.emitSelection();
  }

  toggleParent(node: FlatNode): void {
    const descendants = this.treeControl.getDescendants(node);
    const allSelected = this.descendantsAllSelected(node);
    if (allSelected) {
      this.checklistSelection.deselect(...descendants);
    } else {
      this.checklistSelection.select(...descendants.filter(d => d.privilege));
    }
    this.emitSelection();
  }

  descendantsAllSelected(node: FlatNode): boolean {
    const descendants = this.treeControl.getDescendants(node).filter(d => d.privilege);
    return descendants.length > 0 && descendants.every(d => this.checklistSelection.isSelected(d));
  }

  descendantsPartiallySelected(node: FlatNode): boolean {
    const descendants = this.treeControl.getDescendants(node).filter(d => d.privilege);
    const result = descendants.some(d => this.checklistSelection.isSelected(d));
    return result && !this.descendantsAllSelected(node);
  }

  private emitSelection(): void {
    const selected = this.checklistSelection.selected
      .filter(n => n.privilege)
      .map(n => n.privilege!);
    this.selectionChange.emit(selected);
  }
}
