import { Component, OnInit, OnDestroy, AfterContentInit, ViewChild, ContentChildren, QueryList, input, output, effect, computed } from '@angular/core';
import { MatTableModule, MatTable, MatColumnDef, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription, debounceTime, distinctUntilChanged } from 'rxjs';
import { BlossomSkeletonComponent } from '../skeleton/blossom-skeleton.component';

@Component({
  selector: 'blossom-table',
  standalone: true,
  imports: [MatTableModule, MatPaginatorModule, MatSortModule, MatFormFieldModule, MatInputModule, MatIconModule, MatProgressBarModule, FormsModule, BlossomSkeletonComponent],
  template: `
    <div class="blossom-table-container">
      @if (searchable()) {
        <mat-form-field appearance="outline" class="search-field">
          <mat-label>{{ searchLabel() }}</mat-label>
          <input matInput [ngModel]="searchQuery" (ngModelChange)="onSearchChange($event)">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
      }

      <div class="table-loading-bar">
        @if (loading()) { <mat-progress-bar mode="indeterminate"></mat-progress-bar> }
      </div>

      <div class="table-scroll" [class.refreshing]="refreshing()" [attr.aria-busy]="loading()">
        <table mat-table [dataSource]="dataSource" matSort (matSortChange)="onSortChange($event)">
          <ng-content></ng-content>
          <tr mat-header-row *matHeaderRowDef="displayedColumns()"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns();" (click)="rowClick.emit(row)" class="clickable-row"></tr>
          <tr class="mat-mdc-row" *matNoDataRow>
            <td class="mat-mdc-cell no-data-cell" [attr.colspan]="displayedColumns().length">
              @if (loading()) {
                @for (line of skeletonRows; track line) { <blossom-skeleton class="skeleton-row" height="20px"></blossom-skeleton> }
              } @else {
                <span class="empty-message">{{ emptyMessage() }}</span>
              }
            </td>
          </tr>
        </table>
      </div>

      <mat-paginator
        [length]="totalElements()"
        [pageIndex]="pageIndex()"
        [pageSize]="pageSize()"
        [pageSizeOptions]="pageSizeOptions()"
        (page)="onPageChange($event)"
        showFirstLastButtons>
      </mat-paginator>
    </div>
  `,
  styles: [`
    .blossom-table-container { width: 100%; }
    .table-scroll { width: 100%; overflow-x: auto; transition: opacity 150ms; }
    .table-scroll.refreshing { opacity: 0.5; pointer-events: none; }
    .table-loading-bar { height: 4px; }
    .no-data-cell { padding: 16px; }
    .skeleton-row + .skeleton-row { margin-top: 16px; }
    .empty-message { display: block; text-align: center; color: rgba(127, 127, 127, 0.9); }
    .search-field { width: 100%; margin-bottom: 16px; }
    .clickable-row { cursor: pointer; }
    .clickable-row:hover { background-color: rgba(0, 0, 0, 0.04); }
    table { width: 100%; }
  `]
})
export class BlossomTableComponent implements OnInit, OnDestroy, AfterContentInit {
  displayedColumns = input<string[]>([]);
  searchable = input(true);
  searchLabel = input(typeof navigator !== 'undefined' && navigator.language?.startsWith('fr') ? 'Rechercher...' : 'Search...');
  pageSize = input(25);
  pageSizeOptions = input([10, 25, 50, 100]);
  totalElements = input(0);
  data = input<any[]>([]);
  /** Shows a progress bar, skeleton rows while there is no data yet, the current rows dimmed otherwise */
  loading = input(false);
  emptyMessage = input(typeof navigator !== 'undefined' && navigator.language?.startsWith('fr') ? 'Aucun résultat' : 'No results');
  /** The page shown by the paginator: the parent sets it back to 0 after a search */
  pageIndex = input(0);

  search = output<string>();
  pageChange = output<PageEvent>();
  sortChange = output<Sort>();
  rowClick = output<any>();

  dataSource = new MatTableDataSource<any>();
  readonly skeletonRows = [0, 1, 2, 3, 4];
  refreshing = computed(() => this.loading() && this.data().length > 0);
  private columnsSub: Subscription | undefined;
  searchQuery = '';
  private searchSubject = new Subject<string>();
  private searchSub: Subscription | undefined;

  @ViewChild(MatTable, { static: true }) table!: MatTable<any>;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ContentChildren(MatColumnDef) columnDefs!: QueryList<MatColumnDef>;

  constructor() {
    effect(() => {
      this.dataSource.data = this.data();
    });
  }

  ngAfterContentInit(): void {
    // Register content-projected column definitions with the table, again when they change (columns inside an @if)
    this.columnDefs.forEach(colDef => this.table.addColumnDef(colDef));
    this.columnsSub = this.columnDefs.changes.subscribe((defs: QueryList<MatColumnDef>) =>
      defs.forEach(colDef => this.table.addColumnDef(colDef)));
  }

  ngOnInit(): void {
    this.searchSub = this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(query => this.search.emit(query));
  }

  ngOnDestroy(): void {
    this.searchSub?.unsubscribe();
    this.columnsSub?.unsubscribe();
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
