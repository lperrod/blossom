import { Component, OnInit, OnDestroy, ViewChild, input, output, effect } from '@angular/core';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'blossom-table',
  standalone: true,
  imports: [MatTableModule, MatPaginatorModule, MatSortModule, MatFormFieldModule, MatInputModule, MatIconModule, FormsModule],
  // Note: OnPush is NOT used here because MatTable needs default change detection
  // to properly discover content-projected MatColumnDef directives via ng-content.
  template: `
    <div class="blossom-table-container">
      @if (searchable()) {
        <mat-form-field appearance="outline" class="search-field">
          <mat-label>{{ searchLabel() }}</mat-label>
          <input matInput [ngModel]="searchQuery" (ngModelChange)="onSearchChange($event)">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
      }

      <table mat-table [dataSource]="dataSource" matSort (matSortChange)="onSortChange($event)">
        <ng-content></ng-content>
        <tr mat-header-row *matHeaderRowDef="displayedColumns()"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns();" (click)="rowClick.emit(row)" class="clickable-row"></tr>
      </table>

      <mat-paginator
        [length]="totalElements()"
        [pageSize]="pageSize()"
        [pageSizeOptions]="pageSizeOptions()"
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
export class BlossomTableComponent implements OnInit, OnDestroy {
  displayedColumns = input<string[]>([]);
  searchable = input(true);
  searchLabel = input(navigator.language?.startsWith('fr') ? 'Rechercher...' : 'Search...');
  pageSize = input(25);
  pageSizeOptions = input([10, 25, 50, 100]);
  totalElements = input(0);
  data = input<any[]>([]);

  search = output<string>();
  pageChange = output<PageEvent>();
  sortChange = output<Sort>();
  rowClick = output<any>();

  dataSource = new MatTableDataSource<any>();
  searchQuery = '';
  private searchSubject = new Subject<string>();
  private searchSub: Subscription | undefined;

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor() {
    effect(() => {
      this.dataSource.data = this.data();
    });
  }

  ngOnInit(): void {
    this.searchSub = this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(query => this.search.emit(query));
  }

  ngOnDestroy(): void {
    this.searchSub?.unsubscribe();
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
