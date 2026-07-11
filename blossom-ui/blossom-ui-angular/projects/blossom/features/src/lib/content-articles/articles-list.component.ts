import { Component, OnInit, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { SearchBarComponent } from '@blossom/ui';
import { NotificationService } from '@blossom/core';
import { ArticlesService, ArticleDTO } from './articles.service';

@Component({ selector: 'app-article-create-dialog', standalone: true,
  imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<h2 mat-dialog-title>Create Article</h2><mat-dialog-content><mat-form-field appearance="outline" class="full-width"><mat-label>Name</mat-label><input matInput [(ngModel)]="form.name" required></mat-form-field><mat-form-field appearance="outline" class="full-width"><mat-label>Summary</mat-label><textarea matInput [(ngModel)]="form.summary" rows="3"></textarea></mat-form-field></mat-dialog-content><mat-dialog-actions align="end"><button mat-button (click)="ref.close()">Cancel</button><button mat-flat-button color="primary" (click)="create()" [disabled]="!form.name">Create</button></mat-dialog-actions>`,
  styles: [`.full-width{width:100%;}`]
})
export class ArticleCreateDialogComponent {
  form = { name: '', summary: '' };
  readonly ref = inject(MatDialogRef<ArticleCreateDialogComponent>);
  private svc = inject(ArticlesService);
  private notify = inject(NotificationService);
  create(): void { this.svc.create(this.form).subscribe({ next: a => { this.notify.success('Article created'); this.ref.close(a); }, error: () => this.notify.error('Failed') }); }
}

@Component({ selector: 'app-articles-list', standalone: true,
  imports: [MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule, MatChipsModule, SearchBarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page-header"><h2>Articles</h2><button mat-flat-button color="primary" (click)="openCreate()"><mat-icon>add</mat-icon> Create Article</button></div>
    <blossom-search-bar (search)="onSearch($event)"></blossom-search-bar>
    <table mat-table [dataSource]="articles()" class="full-width">
      <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Name</th><td mat-cell *matCellDef="let a">{{ a.name }}</td></ng-container>
      <ng-container matColumnDef="summary"><th mat-header-cell *matHeaderCellDef>Summary</th><td mat-cell *matCellDef="let a">{{ a.summary }}</td></ng-container>
      <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let a"><mat-chip>{{ a.status }}</mat-chip></td></ng-container>
      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns;" (click)="goTo(row.id)" class="clickable"></tr>
    </table>
    <mat-paginator [length]="total()" [pageSize]="25" (page)="onPage($event)" showFirstLastButtons></mat-paginator>
  `,
  styles: [`.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;}.full-width{width:100%;}.clickable{cursor:pointer;}.clickable:hover{background:rgba(0,0,0,0.04);}`]
})
export class ArticlesListComponent implements OnInit {
  articles = signal<ArticleDTO[]>([]); columns = ['name', 'summary', 'status']; total = signal(0); private q = ''; private pg = 0;
  private svc = inject(ArticlesService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  ngOnInit(): void { this.load(); }
  load(): void { this.svc.list(this.q, this.pg).subscribe(p => { this.articles.set(p.content); this.total.set(p.page.totalElements); }); }
  onSearch(q: string): void { this.q = q; this.pg = 0; this.load(); }
  onPage(e: PageEvent): void { this.pg = e.pageIndex; this.load(); }
  goTo(id: string): void { this.router.navigate(['/content/articles', id]); }
  openCreate(): void { this.dialog.open(ArticleCreateDialogComponent, { width: '500px' }).afterClosed().subscribe(r => { if (r) this.load(); }); }
}
