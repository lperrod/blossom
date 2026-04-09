import { Component, Output, EventEmitter, OnInit, OnDestroy, Input } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'blossom-search-bar',
  standalone: true,
  imports: [MatFormFieldModule, MatInputModule, MatIconModule, FormsModule],
  template: `
    <mat-form-field appearance="outline" class="search-bar">
      <mat-label>{{ placeholder }}</mat-label>
      <input matInput [ngModel]="query" (ngModelChange)="onQueryChange($event)">
      <mat-icon matSuffix>search</mat-icon>
    </mat-form-field>
  `,
  styles: [`.search-bar { width: 100%; }`]
})
export class SearchBarComponent implements OnInit, OnDestroy {
  @Input() placeholder = 'Search...';
  @Input() debounce = 300;
  @Output() search = new EventEmitter<string>();

  query = '';
  private searchSubject = new Subject<string>();
  private subscription!: Subscription;

  ngOnInit(): void {
    this.subscription = this.searchSubject.pipe(
      debounceTime(this.debounce),
      distinctUntilChanged()
    ).subscribe(q => this.search.emit(q));
  }

  onQueryChange(value: string): void {
    this.query = value;
    this.searchSubject.next(value);
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
