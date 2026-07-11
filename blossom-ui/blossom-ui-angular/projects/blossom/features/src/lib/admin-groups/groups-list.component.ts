import { Component, OnInit, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { SearchBarComponent } from '@blossom/ui';
import { NotificationService } from '@blossom/core';
import { GroupsService, GroupDTO } from './groups.service';

@Component({
  selector: 'app-group-create-dialog', standalone: true,
  imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>Create Group</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="full-width"><mat-label>Name</mat-label><input matInput [(ngModel)]="form.name" required></mat-form-field>
      <mat-form-field appearance="outline" class="full-width"><mat-label>Description</mat-label><textarea matInput [(ngModel)]="form.description" rows="3"></textarea></mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close()">Cancel</button>
      <button mat-flat-button color="primary" (click)="create()" [disabled]="!form.name">Create</button>
    </mat-dialog-actions>
  `, styles: [`.full-width{width:100%;}`]
})
export class GroupCreateDialogComponent {
  form = { name: '', description: '' };
  readonly dialogRef = inject(MatDialogRef<GroupCreateDialogComponent>);
  private svc = inject(GroupsService);
  private notify = inject(NotificationService);
  create(): void { this.svc.create(this.form).subscribe({ next: g => { this.notify.success('Group created'); this.dialogRef.close(g); }, error: () => this.notify.error('Failed') }); }
}

@Component({
  selector: 'app-groups-list', standalone: true,
  imports: [MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule, SearchBarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page-header"><h2>Groups</h2><button mat-flat-button color="primary" (click)="openCreate()"><mat-icon>add</mat-icon> Create Group</button></div>
    <blossom-search-bar (search)="onSearch($event)"></blossom-search-bar>
    <table mat-table [dataSource]="groups()" class="full-width">
      <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Name</th><td mat-cell *matCellDef="let g">{{ g.name }}</td></ng-container>
      <ng-container matColumnDef="description"><th mat-header-cell *matHeaderCellDef>Description</th><td mat-cell *matCellDef="let g">{{ g.description }}</td></ng-container>
      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns;" (click)="goTo(row.id)" class="clickable"></tr>
    </table>
    <mat-paginator [length]="total()" [pageSize]="25" (page)="onPage($event)" showFirstLastButtons></mat-paginator>
  `,
  styles: [`.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;}.full-width{width:100%;}.clickable{cursor:pointer;}.clickable:hover{background:rgba(0,0,0,0.04);}`]
})
export class GroupsListComponent implements OnInit {
  groups = signal<GroupDTO[]>([]); columns = ['name', 'description']; total = signal(0); private q = ''; private pg = 0;
  private svc = inject(GroupsService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private notify = inject(NotificationService);
  ngOnInit(): void { this.load(); }
  load(): void { this.svc.list(this.q, this.pg).subscribe(p => { this.groups.set(p.content); this.total.set(p.page.totalElements); }); }
  onSearch(q: string): void { this.q = q; this.pg = 0; this.load(); }
  onPage(e: PageEvent): void { this.pg = e.pageIndex; this.load(); }
  goTo(id: string): void { this.router.navigate(['/administration/groups', id]); }
  openCreate(): void { this.dialog.open(GroupCreateDialogComponent, { width: '500px' }).afterClosed().subscribe(r => { if (r) this.load(); }); }
}
