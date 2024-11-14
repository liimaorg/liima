import { ChangeDetectionStrategy, Component, computed, input, Input, OnDestroy, OnInit, signal } from '@angular/core';

import { NgSelectModule } from '@ng-select/ng-select';
import { Release } from '../../settings/releases/release';
import { FormsModule } from '@angular/forms';
import { Output, EventEmitter } from '@angular/core';
import { ButtonComponent } from '../../shared/button/button.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { toObservable } from '@angular/core/rxjs-interop';
import { skip, Subject, take } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-apps-filter',
  standalone: true,
  imports: [FormsModule, NgSelectModule, ButtonComponent, IconComponent],
  templateUrl: './apps-filter.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppsFilterComponent implements OnDestroy {
  @Input() releases: Release[];

  private destroy$ = new Subject<void>();
  upcoming = input.required<number>();
  selected = signal<number>(undefined);

  @Output() filterEvent = new EventEmitter<{ filter: string; releaseId: number }>();

  appName: string;

  constructor() {
    toObservable(this.upcoming)
      .pipe(takeUntil(this.destroy$))
      .subscribe((release) => {
        if (this.selected() === undefined && release !== null) this.selected.set(release);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  search() {
    this.filterEvent.emit({ filter: this.appName, releaseId: this.selected() });
  }
}
