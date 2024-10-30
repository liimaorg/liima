import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';

import { NgSelectModule } from '@ng-select/ng-select';
import { Release } from '../../settings/releases/release';
import { FormsModule } from '@angular/forms';
import { Output, EventEmitter } from '@angular/core';
import { ButtonComponent } from '../../shared/button/button.component';
import { IconComponent } from '../../shared/icon/icon.component';

@Component({
  selector: 'app-apps-filter',
  standalone: true,
  imports: [FormsModule, NgSelectModule, ButtonComponent, IconComponent],
  templateUrl: './apps-filter.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppsFilterComponent {
  @Input() releases: Release[];
  @Input() selectedReleaseId: number;
  @Output() filterEvent = new EventEmitter<{ filter: string; releaseId: number }>();

  appName: string;

  search() {
    this.filterEvent.emit({ filter: this.appName, releaseId: this.selectedReleaseId });
  }
}
