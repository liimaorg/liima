import { NgbTimeStruct, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import * as datefns from 'date-fns';
import { DATE_FORMAT } from 'src/app/core/amw-constants';

export interface NgbDateTimeStruct extends NgbDateStruct, NgbTimeStruct {}

export class DateModel implements NgbDateStruct {
  year: number;
  month: number;
  day: number;

  timeZoneOffset: number;

  public constructor(init?: Partial<DateModel>) {
    Object.assign(this, init);
  }

  private static fromDate(date: Date): DateModel {
    if (!datefns.isValid(date)) {
      return null;
    }
    return new DateModel({
      year: date.getFullYear(),
      // months start at 0
      month: date.getMonth() + 1,
      // getDate is the day of month
      // getDay is the day of the week
      day: date.getDate(),
      timeZoneOffset: date.getTimezoneOffset(),
    });
  }

  public static fromLocalString(dateString: string, format?: string): DateModel {
    let date: Date;
    if (typeof format === 'undefined') {
      date = datefns.parse(dateString, DATE_FORMAT, new Date());
    } else {
      date = datefns.parse(dateString, format, new Date());
    }
    return this.fromDate(date);
  }

  public static fromEpoch(epoch: number) {
    const date = datefns.toDate(epoch);
    return this.fromDate(date);
  }

  private thisToDate(): Date {
    return new Date(this.year, this.month - 1, this.day);
  }

  public toString(format?: string): string {
    const date = datefns.toDate(this.thisToDate());
    if (!datefns.isValid(date)) {
      return null;
    }
    if (typeof format === 'undefined') {
      return datefns.format(date, DATE_FORMAT);
    }
    return datefns.format(date, format);
  }

  public toEpoch(): number {
    const date = datefns.toDate(this.thisToDate());
    if (!datefns.isValid(date)) {
      return null;
    }
    return date.getTime();
  }

  public toJSON(): string {
    return this.toString();
  }
}
