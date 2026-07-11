import { Component, OnInit, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialog } from '@angular/material/dialog';
import { NotificationService } from '@blossom/core';
import { ConfirmDialogComponent } from '@blossom/ui';
import { GroupsService, GroupDTO } from './groups.service';

@Component({
  selector: 'app-group-detail', standalone: true,
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatTabsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (group()) {
      <div class="page-header"><h2>{{ group()!.name }}</h2>
        <div>
          @if (editing()) {
            <button mat-flat-button color="primary" (click)="save()"><mat-icon>save</mat-icon> Save</button>
          }
          <button mat-button (click)="editing.set(!editing())">{{ editing() ? 'Cancel' : 'Edit' }}</button>
          <button mat-icon-button color="warn" (click)="confirmDelete()"><mat-icon>delete</mat-icon></button>
        </div>
      </div>
      <mat-card><mat-card-content>
        <mat-form-field appearance="outline" class="full-width"><mat-label>Name</mat-label><input matInput [(ngModel)]="group()!.name" [disabled]="!editing()"></mat-form-field>
        <mat-form-field appearance="outline" class="full-width"><mat-label>Description</mat-label><textarea matInput [(ngModel)]="group()!.description" [disabled]="!editing()" rows="3"></textarea></mat-form-field>
      </mat-card-content></mat-card>
    }
  `,
  styles: [`.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;}.full-width{width:100%;}`]
})
export class GroupDetailComponent implements OnInit {
  group = signal<GroupDTO | null>(null);
  editing = signal(false);

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private svc = inject(GroupsService);
  private dialog = inject(MatDialog);
  private notify = inject(NotificationService);

  ngOnInit(): void { this.svc.get(this.route.snapshot.paramMap.get('id')!).subscribe(g => this.group.set(g)); }
  save(): void { if (!this.group()) return; this.svc.update(this.group()!.id, this.group()!).subscribe({ next: g => { this.group.set(g); this.editing.set(false); this.notify.success('Group updated'); }, error: () => this.notify.error('Failed') }); }
  confirmDelete(): void { if (!this.group()) return; this.dialog.open(ConfirmDialogComponent, { data: { title: 'Delete Group', message: `Delete ${this.group()!.name}?`, confirmText: 'Delete' } }).afterClosed().subscribe(r => { if (r) this.svc.delete(this.group()!.id).subscribe({ next: () => { this.notify.success('Group deleted'); this.router.navigate(['/administration/groups']); } }); }); }
}
