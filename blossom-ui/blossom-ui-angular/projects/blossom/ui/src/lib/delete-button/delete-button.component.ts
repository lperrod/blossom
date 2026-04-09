import { Component, Output, EventEmitter } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'blossom-delete-button',
  standalone: true,
  imports: [MatButtonModule, MatIconModule],
  template: `
    <button mat-icon-button color="warn" (click)="confirmDelete()">
      <mat-icon>delete</mat-icon>
    </button>
  `
})
export class DeleteButtonComponent {
  @Output() confirmed = new EventEmitter<void>();

  constructor(private dialog: MatDialog) {}

  confirmDelete(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirm Delete',
        message: 'Are you sure you want to delete this item?',
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.confirmed.emit();
      }
    });
  }
}
