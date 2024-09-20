import { Component, Input } from '@angular/core';

import { NgSelectModule } from '@ng-select/ng-select';
import { Release } from '../../settings/releases/release';
import { FormsModule } from '@angular/forms';
import { Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'amw-apps-filter',
  standalone: true,
  imports: [FormsModule, NgSelectModule],
  templateUrl: './apps-filter.component.html',
})
export class AppsFilterComponent {
  @Input() releases: Release[];

  @Output() filterEvent = new EventEmitter<{ filter: string; releaseId: number }>();

  releaseId: number = 50;
  appName: string;

  search() {
    this.filterEvent.emit({ filter: this.appName, releaseId: this.releaseId });
  }
}
