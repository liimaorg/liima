import { Component, input, output } from '@angular/core';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbDatepicker, NgbPopover, NgbTimepicker } from '@ng-bootstrap/ng-bootstrap';
import { IconComponent } from '../icon/icon.component';
import { ButtonComponent } from '../button/button.component';
@Component({
  selector: 'app-tile-component',
  templateUrl: './tile.component.html',
  styleUrls: ['./tile.component.scss'],
  providers: [],
  standalone: true,
  imports: [FormsModule, NgClass, NgbPopover, IconComponent, NgbDatepicker, NgbTimepicker, ButtonComponent],
})
export class TileComponent {
  title = input.required<string>();
  action = input.required<string>();
  tileAction = output<void>();

  onAction(): void {
    this.tileAction.emit();
  }
}
