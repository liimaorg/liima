import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, inject, Input, OnInit, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DeploymentFilter } from '../../deployment/deployment-filter';
import { ComparatorFilterOption } from '../../deployment/comparator-filter-option';
import { DeploymentService } from '../../deployment/deployment.service';
import { ButtonComponent } from '../../shared/button/button.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { DateTimePickerComponent } from '../../shared/date-time-picker/date-time-picker.component';

@Component({
  selector: 'app-deployment-filter',
  templateUrl: './deployment-filter.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, ButtonComponent, IconComponent, DateTimePickerComponent],
})
export class DeploymentFilterComponent implements OnInit {
  private deploymentService = inject(DeploymentService);

  @Input({ required: true }) filter!: DeploymentFilter;
  @Input({ required: true }) index!: number;
  @Input({ required: true }) type!: string;
  @Input() compOptions: ComparatorFilterOption[] = [];
  @Output() remove = new EventEmitter<DeploymentFilter>();

  valOptions = signal<string[]>([]);

  ngOnInit() {
    // Pre-seed options with the current value so the select can render it before async options load
    if (this.filter?.val) {
      this.valOptions.set([String(this.filter.val)]);
    }
    this.loadOptions();
  }

  private loadOptions() {
    if (this.type === 'booleanType') {
      this.valOptions.set(['true', 'false']);
    } else if (this.type !== 'SpecialFilterType' && this.type !== 'DateType') {
      this.deploymentService.getFilterOptionValues(this.filter.name).subscribe({
        next: (options) => {
          this.valOptions.set(options);
        },
      });
    }
  }

  onRemove() {
    this.remove.emit(this.filter);
  }
}
