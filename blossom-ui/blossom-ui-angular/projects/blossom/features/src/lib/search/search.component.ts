import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { RouterModule } from '@angular/router';
import { SearchBarComponent } from '@blossom/ui';
import { SearchService } from './search.service';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [MatListModule, MatIconModule, MatPaginatorModule, RouterModule, SearchBarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2>Search</h2>
    <blossom-search-bar placeholder="Search across all entities..." [debounce]="500" (search)="onSearch($event)"></blossom-search-bar>
    @if (results()) {
      <div>
        <mat-list>
          @for (item of results().content; track item.id) {
            <mat-list-item>
              <mat-icon matListItemIcon>{{ item.type === 'USER' ? 'person' : item.type === 'GROUP' ? 'group' : item.type === 'ROLE' ? 'vpn_key' : 'description' }}</mat-icon>
              <span matListItemTitle>{{ item.name || item.identifier || item.id }}</span>
              <span matListItemLine>{{ item.type }}</span>
            </mat-list-item>
          }
        </mat-list>
        <mat-paginator
          [length]="results().page.totalElements"
          [pageSize]="pageSize"
          (page)="onPage($event)"
          showFirstLastButtons>
        </mat-paginator>
      </div>
    }
    @if (query && !results()?.content?.length) {
      <p>No results found.</p>
    }
  `
})
export class SearchComponent {
  query = '';
  results = signal<any>(null);
  pageSize = 25;

  private searchService = inject(SearchService);

  onSearch(q: string): void {
    this.query = q;
    if (q) { this.doSearch(0); }
    else { this.results.set(null); }
  }

  onPage(event: PageEvent): void { this.doSearch(event.pageIndex); }

  private doSearch(page: number): void {
    this.searchService.search(this.query, page, this.pageSize).subscribe(r => this.results.set(r));
  }
}
