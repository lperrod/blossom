import { Component, Input, Output, EventEmitter, ViewChild, AfterViewInit, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'blossom-table',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatPaginatorModule, MatSortModule, MatFormFieldModule, MatInputModule, MatIconModule, FormsModule],
  template: `
    <div class="blossom-table-container">
      <mat-form-field *ngIf="searchable" appearance="outline" class="search-field">
        <mat-label>Search</mat-label>
        <input matInput [ngModel]="searchQuery" (ngModelChange)="onSearchChange($event)" placeholder="Search...">
        <mat-icon matSuffix>search</mat-icon>
      </mat-form-field>

      <table mat-table [dataSource]="dataSource" matSort (matSortChange)="onSortChange($event)">
        <ng-content></ng-content>
        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;" (click)="rowClick.emit(row)" class="clickable-row"></tr>
      </table>

      <mat-paginator
        [length]="totalElements"
        [pageSize]="pageSize"
        [pageSizeOptions]="pageSizeOptions"
        (page)="onPageChange($event)"
        showFirstLastButtons>
      </mat-paginator>
    </div>
  `,
  styles: [`
    .blossom-table-container { width: 100%; }
    .search-field { width: 100%; margin-bottom: 16px; }
    .clickable-row { cursor: pointer; }
    .clickable-row:hover { background-color: rgba(0, 0, 0, 0.04); }
    table { width: 100%; }
  `]
})
export class BlossomTableComponent implements OnInit {
  @Input() displayedColumns: string[] = [];
  @Input() searchable = true;
  @Input() pageSize = 25;
  @Input() pageSizeOptions = [10, 25, 50, 100];
  @Input() totalElements = 0;

  @Input() set data(value: any[]) {
    this.dataSource.data = value;
  }

  @Output() search = new EventEmitter<string>();
  @Output() pageChange = new EventEmitter<PageEvent>();
  @Output() sortChange = new EventEmitter<Sort>();
  @Output() rowClick = new EventEmitter<any>();

  dataSource = new MatTableDataSource<any>();
  searchQuery = '';
  private searchSubject = new Subject<string>();

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(query => this.search.emit(query));
  }

  onSearchChange(query: string): void {
    this.searchQuery = query;
    this.searchSubject.next(query);
  }

  onPageChange(event: PageEvent): void {
    this.pageChange.emit(event);
  }

  onSortChange(sort: Sort): void {
    this.sortChange.emit(sort);
  }
}
