import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { SearchBarComponent } from '@blossom/ui';
import { NotificationService } from '@blossom/core';
import { FileManagerService, FileDTO } from './filemanager.service';

@Component({
  selector: 'app-filemanager', standalone: true,
  imports: [CommonModule, MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule, MatChipsModule, SearchBarComponent],
  template: `
    <div class="page-header">
      <h2>File Manager</h2>
      <button mat-flat-button color="primary" (click)="fileInput.click()"><mat-icon>upload</mat-icon> Upload</button>
      <input #fileInput type="file" hidden (change)="onFileSelected($event)">
    </div>
    <blossom-search-bar (search)="onSearch($event)"></blossom-search-bar>
    <table mat-table [dataSource]="files" class="full-width">
      <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Name</th><td mat-cell *matCellDef="let f">{{ f.name }}</td></ng-container>
      <ng-container matColumnDef="contentType"><th mat-header-cell *matHeaderCellDef>Type</th><td mat-cell *matCellDef="let f">{{ f.contentType }}</td></ng-container>
      <ng-container matColumnDef="size"><th mat-header-cell *matHeaderCellDef>Size</th><td mat-cell *matCellDef="let f">{{ formatSize(f.size) }}</td></ng-container>
      <ng-container matColumnDef="extension"><th mat-header-cell *matHeaderCellDef>Extension</th><td mat-cell *matCellDef="let f"><mat-chip>{{ f.extension }}</mat-chip></td></ng-container>
      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns;"></tr>
    </table>
    <mat-paginator [length]="total" [pageSize]="25" (page)="onPage($event)" showFirstLastButtons></mat-paginator>
  `,
  styles: [`.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;gap:16px;}.full-width{width:100%;}`]
})
export class FileManagerComponent implements OnInit {
  files: FileDTO[] = []; columns = ['name', 'contentType', 'size', 'extension']; total = 0; private q = ''; private pg = 0;
  constructor(private svc: FileManagerService, private notify: NotificationService) {}
  ngOnInit(): void { this.load(); }
  load(): void { this.svc.list(this.q, this.pg).subscribe(p => { this.files = p.content; this.total = p.page.totalElements; }); }
  onSearch(q: string): void { this.q = q; this.pg = 0; this.load(); }
  onPage(e: PageEvent): void { this.pg = e.pageIndex; this.load(); }
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) { this.svc.upload(file).subscribe({ next: () => { this.notify.success('File uploaded'); this.load(); }, error: () => this.notify.error('Upload failed') }); }
  }
  formatSize(bytes: number): string {
    if (!bytes) return '0 B'; const k = 1024; const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }
}
