import { Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.component.html',
  imports: [FormsModule],
})
export class PaginationComponent {
  currentPage = input.required<number>();
  lastPage = input.required<number>();
  doSetOffset = output<number>();
  doSetMax = output<number>();

  paginatorItems: number = 5;

  maxResults: number = 10;

  pages(): number[] {
    if (this.lastPage() > 1) {
      const itemsBefore: number = Math.floor(this.paginatorItems / 2);
      const start: number = this.currentPage() > itemsBefore ? this.currentPage() - itemsBefore : 1;
      const end: number = start + this.paginatorItems - 1;
      return this.range(start, end < this.lastPage() ? end : this.lastPage());
    }
    return;
  }

  toPage(page: number) {
    if (page <= this.lastPage() && page !== this.currentPage()) {
      page = page > 0 ? page - 1 : 0;
      this.doSetOffset.emit(page * this.maxResults);
    }
  }

  setMax() {
    this.doSetMax.emit(this.maxResults);
  }

  private range(a: number, b: number): number[] {
    const d: number[] = [];
    let c: number = b - a + 1;
    while (c--) {
      d[c] = b--;
    }
    return d;
  }
}
