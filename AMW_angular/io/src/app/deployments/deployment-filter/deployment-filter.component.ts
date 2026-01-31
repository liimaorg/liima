import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DeploymentFilter } from '../../deployment/deployment-filter';
import { ComparatorFilterOption } from '../../deployment/comparator-filter-option';
import { DeploymentService } from '../../deployment/deployment.service';
import { FilterType } from '../../deployment/filter-type.enum';
import { ButtonComponent } from '../../shared/button/button.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { DateTimePickerComponent } from '../../shared/date-time-picker/date-time-picker.component';

@Component({
  selector: 'app-deployment-filter',
  templateUrl: './deployment-filter.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule, ButtonComponent, IconComponent, DateTimePickerComponent],
})
export class DeploymentFilterComponent implements OnInit {
  private deploymentService = inject(DeploymentService);
  protected readonly FilterType = FilterType;

  filter = input.required<DeploymentFilter>();
  index = input.required<number>();
  type = input.required<string>();
  compOptions = input<ComparatorFilterOption[]>([]);
  remove = output<DeploymentFilter>();

  valOptions = signal<string[]>([]);

  ngOnInit() {
    // Pre-seed options with the current value so the select can render it before async options load
    if (this.filter()?.val) {
      this.valOptions.set([String(this.filter().val)]);
    }
    this.loadOptions();
  }

  private loadOptions() {
    if (this.type() === FilterType.BOOLEAN) {
      this.valOptions.set(['true', 'false']);
    } else if (this.type() !== FilterType.SPECIAL && this.type() !== FilterType.DATE) {
      this.deploymentService.getFilterOptionValues(this.filter().name).subscribe({
        next: (options) => {
          this.valOptions.set(options);
        },
      });
    }
  }

  onRemove() {
    this.remove.emit(this.filter());
  }
}
