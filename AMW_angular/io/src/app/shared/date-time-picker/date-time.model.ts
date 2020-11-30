import { NgbTimeStruct, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import * as moment from 'moment';

export interface NgbDateTimeStruct extends NgbDateStruct, NgbTimeStruct {}

export class DateTimeModel implements NgbDateTimeStruct {
  year: number;
  month: number;
  day: number;
  hour: number;
  minute: number;
  second: number;

  timeZoneOffset: number;

  public constructor(init?: Partial<DateTimeModel>) {
    Object.assign(this, init);
  }

  public static fromLocalString(dateString: string, format: string): DateTimeModel {
    const date = moment(dateString, format).toDate();
    const isValidDate = !isNaN(date.valueOf());

    if (!dateString || !isValidDate) {
      return null;
    }

    return new DateTimeModel({
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      day: date.getDate(),
      hour: date.getHours(),
      minute: date.getMinutes(),
      second: date.getSeconds(),
      timeZoneOffset: date.getTimezoneOffset(),
    });
  }

  public static fromEpoch(epoch: number) {
    debugger;
    const date = moment(epoch).toDate();
    return new DateTimeModel({
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      day: date.getDate(),
      hour: date.getHours(),
      minute: date.getMinutes(),
      second: date.getSeconds(),
      timeZoneOffset: date.getTimezoneOffset(),
    });
  }

  public toString(format: string): string {
    const m = moment({ year: this.year, month: this.month - 1, day: this.day, hour: this.hour, minute: this.minute, second: this.second});
    return m.format(format);
  }

  public toEpoch(): number {
    const m = moment({ year: this.year, month: this.month - 1, day: this.day, hour: this.hour, minute: this.minute, second: this.second});
    return m.valueOf();
  }
}
