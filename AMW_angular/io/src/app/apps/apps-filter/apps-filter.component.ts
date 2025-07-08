import { ChangeDetectionStrategy, Component, computed, input, OnChanges, output, signal, SimpleChanges } from '@angular/core';

import { NgSelectModule } from '@ng-select/ng-select';
import { Release } from '../../settings/releases/release';
import { FormsModule } from '@angular/forms';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-apps-filter',
  imports: [FormsModule, NgSelectModule, ButtonComponent],
  templateUrl: './apps-filter.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppsFilterComponent implements OnChanges {
  releases = input.required<Release[]>();
  releaseId = input.required<number>();
  filter = input<string>();
  filterEvent = output<{ filter: string; releaseId: number }>();

  filterValue = signal<string>('');
  selectedReleaseId = signal<number>(0);

  selection = computed(() => {
    return {
      releases: this.releases(),
      selected: this.selectedReleaseId,
    };
  });

  ngOnChanges(changes: SimpleChanges) {
    if (changes.filter) {
      this.filterValue.set(this.filter() ?? '');
    }
    if (changes.releaseId) {
      this.selectedReleaseId.set(this.releaseId());
    }
  }

  search() {
    this.filterEvent.emit({ filter: this.filterValue(), releaseId: this.selectedReleaseId() });
  }

}
