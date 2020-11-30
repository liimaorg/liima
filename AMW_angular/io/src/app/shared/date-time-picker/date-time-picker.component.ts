import {
  Component,
  OnInit,
  Input,
  forwardRef,
  ViewChild,
  AfterViewInit,
  Injector,
} from '@angular/core';
import {
  NgbTimeStruct,
  NgbPopoverConfig,
  NgbPopover,
  NgbDatepicker,
} from '@ng-bootstrap/ng-bootstrap';
import {
  NG_VALUE_ACCESSOR,
  ControlValueAccessor,
  NgControl,
} from '@angular/forms';
import { DatePipe } from '@angular/common';
import { DateTimeModel } from './date-time.model';
import { noop } from 'rxjs';

@Component({
  selector: 'app-date-time-picker',
  templateUrl: './date-time-picker.component.html',
  styleUrls: ['./date-time-picker.component.scss'],
  providers: [
    DatePipe,
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DateTimePickerComponent),
      multi: true,
    },
  ],
})
export class DateTimePickerComponent
  implements ControlValueAccessor, OnInit, AfterViewInit {
  @Input()
  dateString: string;

  // moment js format
  @Input()
  dateStringFormat = 'DD.MM.yyyy HH:mm';
  @Input()
  hourStep = 1;
  @Input()
  minuteStep = 15;
  @Input()
  secondStep = 30;
  @Input()
  seconds = false;
  @Input()
  disabled = false;

  showTimePickerToggle = false;
  datetime = new DateTimeModel();

  @ViewChild(NgbDatepicker)
  dp: NgbDatepicker;

  @ViewChild(NgbPopover)
  popover: NgbPopover;

  onTouched: () => void = noop;
  onChange: (_: any) => void = noop;

  ngControl: NgControl;

  constructor(private config: NgbPopoverConfig, private inj: Injector) {
    config.autoClose = 'outside';
    config.placement = 'auto';
  }

  ngOnInit(): void {
    this.ngControl = this.inj.get(NgControl);
  }

  ngAfterViewInit(): void {
    this.popover.hidden.subscribe(($event) => {
      this.showTimePickerToggle = false;
    });
  }

  writeValue(newModel: DateTimeModel) {
    debugger;
    if (newModel) {
      this.datetime = Object.assign(this.datetime, newModel)
      //this.datetime = newModel;
      this.setDateString();
    } else {
      this.datetime = new DateTimeModel();
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  toggleDateTimeState($event) {
    this.showTimePickerToggle = !this.showTimePickerToggle;
    $event.stopPropagation();
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  // called when user updates the datestring directly
  onDateStringChange($event: any) {
    const value = $event.target.value;
    const dt = DateTimeModel.fromLocalString(value, this.dateStringFormat);

    //todo: handle invalid datestring
    if (dt) {
      this.datetime = dt;
      this.onChange(dt);
    } else if (value.trim() === '') {
      this.datetime = new DateTimeModel();
      this.dateString = '';
      this.onChange(this.datetime);
    }
  }

  onDateChange($event) {
    debugger;
    this.datetime.year = $event.year;
    this.datetime.month = $event.month;
    this.datetime.day = $event.day;

    this.dp.navigateTo({
      year: this.datetime.year,
      month: this.datetime.month,
    });
    this.setDateString();
  }

  onTimeChange(event: NgbTimeStruct) {
    this.datetime.hour = event.hour;
    this.datetime.minute = event.minute;
    this.datetime.second = event.second;

    this.setDateString();
  }

  setDateString() {
    this.dateString = this.datetime.toString(this.dateStringFormat);
    this.onChange(this.datetime);
  }

  inputBlur($event) {
    this.onTouched();
  }
}
