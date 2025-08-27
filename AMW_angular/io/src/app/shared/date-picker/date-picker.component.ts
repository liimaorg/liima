import { Component, OnInit, Input, forwardRef, ViewChild, Injector, inject } from '@angular/core';
import { NgbPopoverConfig, NgbPopover, NgbDateStruct, NgbDatepicker } from '@ng-bootstrap/ng-bootstrap';
import { NG_VALUE_ACCESSOR, ControlValueAccessor, NgControl, FormsModule } from '@angular/forms';
import { DatePipe, NgClass } from '@angular/common';
import { DateModel } from './date.model';
import { noop } from 'rxjs';
import { DATE_FORMAT } from 'src/app/core/amw-constants';
import { IconComponent } from '../icon/icon.component';
import { ButtonComponent } from '../button/button.component';

@Component({
  selector: 'app-date-picker',
  templateUrl: './date-picker.component.html',
  providers: [
    DatePipe,
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DatePickerComponent),
      multi: true,
    },
  ],
  imports: [FormsModule, NgClass, NgbPopover, IconComponent, NgbDatepicker, ButtonComponent],
})
export class DatePickerComponent implements ControlValueAccessor, OnInit {
  private config = inject(NgbPopoverConfig);
  private inj = inject(Injector);

  @Input()
  dateStringFormat = DATE_FORMAT;
  @Input()
  disabled = false;

  dateString = '';

  date = new DateModel();
  errorMessage = '';

  @ViewChild(NgbPopover)
  popover: NgbPopover;

  onTouched: () => void = noop;
  onChange: (_: any) => void = noop;

  ngControl: NgControl;

  constructor() {
    const config = this.config;

    config.autoClose = 'outside';
    config.placement = 'auto';
  }

  ngOnInit(): void {
    this.ngControl = this.inj.get(NgControl);
  }

  writeValue(newModel: DateModel) {
    if (newModel) {
      this.date = Object.assign(this.date, newModel);
      //this.datetime = newModel;
      this.setDateString();
    } else {
      this.date = new DateModel();
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  toggleDateTimeState($event) {
    $event.stopPropagation();
    this.popover.close(true);
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  // called when user updates the datestring directly
  onDateStringChange($event: any) {
    const value = $event.target.value;
    const dt = DateModel.fromLocalString(value, this.dateStringFormat);
    this.errorMessage = '';

    if (dt) {
      this.date = dt;
      this.onChange(dt);
    } else if (value.trim() === '') {
      this.date = new DateModel();
      this.dateString = '';
      this.onChange(this.date);
    } else {
      this.errorMessage = 'Invalid date!';
    }
  }

  onDateChange(event: NgbDateStruct) {
    if (!event) {
      return;
    }
    this.date.year = event.year;
    this.date.month = event.month;
    this.date.day = event.day;
    this.setDateString();
  }

  setDateString() {
    this.dateString = this.date.toString(this.dateStringFormat);
    this.onChange(this.date);
  }

  inputBlur() {
    this.onTouched();
  }

  cancel() {
    this.errorMessage = '';
  }
}
