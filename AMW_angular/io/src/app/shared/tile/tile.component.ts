import { Component, input, OnInit, output, viewChild, ViewContainerRef } from '@angular/core';
import { NgClass, NgComponentOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbDatepicker, NgbPopover, NgbTimepicker } from '@ng-bootstrap/ng-bootstrap';
import { IconComponent } from '../icon/icon.component';
import { ButtonComponent } from '../button/button.component';
import { TileListComponent, TileListInputs } from './tile-list/tile-list.component';
@Component({
  selector: 'app-tile-component',
  templateUrl: './tile.component.html',
  styleUrls: ['./tile.component.scss'],
  providers: [],
  standalone: true,
  imports: [
    FormsModule,
    NgClass,
    NgbPopover,
    NgComponentOutlet,
    IconComponent,
    NgbDatepicker,
    NgbTimepicker,
    ButtonComponent,
  ],
})
export class TileComponent implements OnInit {
  vcr = viewChild('container', { read: ViewContainerRef });

  title = input.required<string>();
  action = input.required<string>();
  data = input<{ component: TileListComponent; inputs: TileListInputs }[]>();

  tileAction = output<void>();

  listAction = output<string>();

  ngOnInit(): void {
    if (this.data()) {
      this.data().map((one) => {
        const componentRef = this.vcr()?.createComponent(TileListComponent);
        componentRef?.setInput('title', one.inputs.title);
        componentRef?.setInput('data', one.inputs.data);
        componentRef?.instance.edit.subscribe((listItem) => this.listAction.emit(listItem));
        componentRef?.instance.delete.subscribe((listItem) => this.listAction.emit(listItem));
        componentRef?.instance.overwrite.subscribe((listItem) => this.listAction.emit(listItem));
      });
    }
  }
}
