import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';

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
export class AppsFilterComponent {
  releases = input.required<Release[]>();
  upcoming = input.required<number>();

  selection = computed(() => {
    return {
      releases: this.releases(),
      selected: signal(this.upcoming()),
    };
  });

  appName = signal<string>('');

  filterEvent = output<{ filter: string; releaseId: number }>();

  search() {
    this.filterEvent.emit({ filter: this.appName(), releaseId: this.selection().selected() });
  }
}
