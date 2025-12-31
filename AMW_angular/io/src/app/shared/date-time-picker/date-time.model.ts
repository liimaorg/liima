import { NgbTimeStruct, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import * as datefns from 'date-fns';
import { DATE_TIME_FORMAT } from 'src/app/core/amw-constants';

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

  private static fromDate(date: Date): DateTimeModel {
    if (!datefns.isValid(date)) {
      return null;
    }
    return new DateTimeModel({
      year: date.getFullYear(),
      // months start at 0
      month: date.getMonth() + 1,
      // getDate is the day of month
      // getDay is the day of the week
      day: date.getDate(),
      hour: date.getHours(),
      minute: date.getMinutes(),
      second: date.getSeconds(),
      timeZoneOffset: date.getTimezoneOffset(),
    });
  }

  public static fromLocalString(dateString: string, format?: string): DateTimeModel {
    let date: Date;
    if (typeof format === 'undefined') {
      date = datefns.parse(dateString, DATE_TIME_FORMAT, new Date());
    } else {
      date = datefns.parse(dateString, format, new Date());
    }
    return this.fromDate(date);
  }

  public static fromEpoch(epoch: number) {
    const date = new Date(epoch);
    return this.fromDate(date);
  }

  private thisToDate(): Date {
    return new Date(this.year, this.month - 1, this.day, this.hour, this.minute, this.second);
  }

  public toString(format?: string): string {
    const date = this.thisToDate();
    if (!datefns.isValid(date)) {
      return null;
    }
    if (typeof format === 'undefined') {
      return datefns.format(date, DATE_TIME_FORMAT);
    }
    return datefns.format(date, format);
  }

  public toEpoch(): number {
    const date = this.thisToDate();
    if (!datefns.isValid(date)) {
      return null;
    }
    return date.getTime();
  }

  public toJSON(): string {
    return this.toString();
  }
}
