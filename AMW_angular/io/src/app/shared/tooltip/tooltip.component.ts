import { Component, input, signal, ViewChild, ViewEncapsulation } from '@angular/core';
import { NgbPopover, NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';

export interface TooltipTableEntry {
  col1: string | number;
  col2: string | number;
  col3?: string | number;
}

@Component({
  selector: 'app-tooltip',
  standalone: true,
  imports: [NgbPopoverModule],
  templateUrl: './tooltip.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrl: './tooltip.component.scss',
})
export class TooltipComponent {
  @ViewChild('popover') popover!: NgbPopover;

  // Inputs
  title = input<string>('');
  subtitle = input<string>('');
  headers = input<string[]>(undefined);
  tableData = input<TooltipTableEntry[]>([]);
  canPin = input<boolean>(true);

  isPinned = signal<boolean>(true);
  private closeTimer: number | null = null;

  handleToggle(event: MouseEvent) {
    event.stopPropagation();
    event.preventDefault();

    this.clearTimer();

    if (this.canPin()) {
      // When canPin is true, toggle the pinned state
      this.isPinned.set(this.isPinned());
      if (this.isPinned()) {
        this.popover.open();
      } else {
        this.popover.close();
      }
    } else {
      // When canPin is false, just toggle open/close without changing pinned state
      if (this.popover.isOpen()) {
        this.popover.close();
      } else {
        this.popover.open();
      }
    }
  }

  handleMouseEnter() {
    // Only open on mouseover if canPin is false
    if (this.canPin()) return;

    this.clearTimer();
    if (!this.popover.isOpen()) {
      this.popover.open();
    }
  }

  handleMouseLeave() {
    if (this.canPin() && this.isPinned()) {
      return;
    }

    this.clearTimer();
    this.closeTimer = setTimeout(() => {
      this.popover.close();
    }, 250);
  }

  clearTimer() {
    if (this.closeTimer) {
      clearTimeout(this.closeTimer);
      this.closeTimer = null;
    }
  }
}
