import { Component, input, signal, ViewChild } from '@angular/core';
import { NgbPopover, NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';

export interface TooltipTableEntry {
  label: string;
  value: string | number;
}
@Component({
  selector: 'app-tooltip',
  standalone: true,
  imports: [NgbPopoverModule],
  templateUrl: './tooltip.component.html',
  styles: [
    `
      :host {
        display: inline-flex;
        align-items: center;
        cursor: pointer;
      }
    `,
  ],
})
export class TooltipComponent {
  @ViewChild('popover') popover!: NgbPopover;

  // Inputs
  title = input<string>('');
  subtitle = input<string>('');
  tableData = input<TooltipTableEntry[]>([]);
  canPin = input<boolean>(true);

  isPinned = signal<boolean>(true);
  private closeTimer: number | null = null;

  handleToggle(event: MouseEvent) {
    event.stopPropagation();

    // If canPin is false, clicking does nothing to the state
    if (!this.canPin()) return;

    this.clearTimer();
    this.isPinned.set(!this.isPinned);

    if (this.isPinned()) {
      this.popover.open();
    } else {
      this.popover.close();
    }
  }

  handleMouseEnter() {
    this.clearTimer();
    if (!this.popover.isOpen()) {
      this.popover.open();
    }
  }

  handleMouseLeave() {
    // CRITICAL FIX: Only block closing if we are ALLOWED to pin AND we ARE pinned.
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
