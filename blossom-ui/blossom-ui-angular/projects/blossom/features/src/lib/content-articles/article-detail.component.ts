import { Component, OnInit, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog } from '@angular/material/dialog';
import { NotificationService } from '@blossom/core';
import { ConfirmDialogComponent } from '@blossom/ui';
import { ArticlesService, ArticleDTO } from './articles.service';

@Component({
  selector: 'app-article-detail', standalone: true,
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatTabsModule, MatSelectModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (article()) {
      <div class="page-header"><h2>{{ article()!.name }}</h2>
        <div>
          @if (editing()) {
            <button mat-flat-button color="primary" (click)="save()"><mat-icon>save</mat-icon> Save</button>
          }
          <button mat-button (click)="editing.set(!editing())">{{ editing() ? 'Cancel' : 'Edit' }}</button>
          <button mat-icon-button color="warn" (click)="confirmDelete()"><mat-icon>delete</mat-icon></button>
        </div>
      </div>
      <mat-tab-group>
        <mat-tab label="Information"><div class="tab-content"><mat-card><mat-card-content>
          <mat-form-field appearance="outline" class="full-width"><mat-label>Name</mat-label><input matInput [(ngModel)]="article()!.name" [disabled]="!editing()"></mat-form-field>
          <mat-form-field appearance="outline" class="full-width"><mat-label>Summary</mat-label><textarea matInput [(ngModel)]="article()!.summary" [disabled]="!editing()" rows="3"></textarea></mat-form-field>
          <mat-form-field appearance="outline" class="full-width"><mat-label>Status</mat-label>
            <mat-select [(ngModel)]="article()!.status" [disabled]="!editing()">
              <mat-option value="DRAFT">Draft</mat-option><mat-option value="PUBLISHED">Published</mat-option><mat-option value="ARCHIVED">Archived</mat-option>
            </mat-select>
          </mat-form-field>
        </mat-card-content></mat-card></div></mat-tab>
        <mat-tab label="Content"><div class="tab-content"><mat-card><mat-card-content>
          <mat-form-field appearance="outline" class="full-width"><mat-label>Content</mat-label><textarea matInput [(ngModel)]="article()!.content" [disabled]="!editing()" rows="15"></textarea></mat-form-field>
        </mat-card-content></mat-card></div></mat-tab>
      </mat-tab-group>
    }
  `,
  styles: [`.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;}.tab-content{padding:16px 0;}.full-width{width:100%;}`]
})
export class ArticleDetailComponent implements OnInit {
  article = signal<ArticleDTO | null>(null);
  editing = signal(false);

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private svc = inject(ArticlesService);
  private dialog = inject(MatDialog);
  private notify = inject(NotificationService);

  ngOnInit(): void { this.svc.get(this.route.snapshot.paramMap.get('id')!).subscribe(a => this.article.set(a)); }
  save(): void { if (!this.article()) return; this.svc.update(this.article()!.id, this.article()!).subscribe({ next: a => { this.article.set(a); this.editing.set(false); this.notify.success('Article updated'); }, error: () => this.notify.error('Failed') }); }
  confirmDelete(): void { if (!this.article()) return; this.dialog.open(ConfirmDialogComponent, { data: { title: 'Delete Article', message: `Delete ${this.article()!.name}?`, confirmText: 'Delete' } }).afterClosed().subscribe(r => { if (r) this.svc.delete(this.article()!.id).subscribe({ next: () => { this.notify.success('Article deleted'); this.router.navigate(['/content/articles']); } }); }); }
}
