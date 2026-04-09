import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { RouterModule } from '@angular/router';
import { SearchBarComponent } from '@blossom/ui';
import { SearchService } from './search.service';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, MatListModule, MatIconModule, MatPaginatorModule, RouterModule, SearchBarComponent],
  template: `
    <h2>Search</h2>
    <blossom-search-bar placeholder="Search across all entities..." [debounce]="500" (search)="onSearch($event)"></blossom-search-bar>
    <div *ngIf="results">
      <mat-list>
        <mat-list-item *ngFor="let item of results.content">
          <mat-icon matListItemIcon>{{ item.type === 'USER' ? 'person' : item.type === 'GROUP' ? 'group' : item.type === 'ROLE' ? 'vpn_key' : 'description' }}</mat-icon>
          <span matListItemTitle>{{ item.name || item.identifier || item.id }}</span>
          <span matListItemLine>{{ item.type }}</span>
        </mat-list-item>
      </mat-list>
      <mat-paginator
        [length]="results.page.totalElements"
        [pageSize]="pageSize"
        (page)="onPage($event)"
        showFirstLastButtons>
      </mat-paginator>
    </div>
    <p *ngIf="query && !results?.content?.length">No results found.</p>
  `
})
export class SearchComponent {
  query = '';
  results: any;
  pageSize = 25;

  constructor(private searchService: SearchService) {}

  onSearch(q: string): void {
    this.query = q;
    if (q) { this.doSearch(0); }
    else { this.results = null; }
  }

  onPage(event: PageEvent): void { this.doSearch(event.pageIndex); }

  private doSearch(page: number): void {
    this.searchService.search(this.query, page, this.pageSize).subscribe(r => this.results = r);
  }
}
