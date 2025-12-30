import { NgbTimeStruct, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import * as datefns from 'date-fns';
import { DATE_FORMAT, ISO_FORMAT } from 'src/app/core/amw-constants';

export interface NgbDateTimeStruct extends NgbDateStruct, NgbTimeStruct {}

// NOTE: Be careful how dates are parsed and constructed!
// Dates should always be handled in UTC. If dates are parsed or constructed in local time (e.g., UTC+2),
// a date can become the previous day when interpreted as UTC. E.g. 1753999200000
//   Your time zone: Friday, 1 August 2025 00:00:00 GMT+02:00 DST
//   GMT: Thursday, 31 July 2025 22:00:00
// => Can be stored as 31 July 2025 in the database!
export class DateModel implements NgbDateStruct {
  year: number;
  month: number;
  day: number;

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
    const date = new Date(epoch);
    return this.fromDate(date);
  }

  private thisToDate(): Date {
    return new Date(this.year, this.month - 1, this.day);
  }

  public toString(format?: string): string {
    const date = this.thisToDate();
    if (!datefns.isValid(date)) {
      return null;
    }
    if (typeof format === 'undefined') {
      return datefns.format(date, DATE_FORMAT);
    }
    return datefns.format(date, format);
  }

  public toEpoch(): number {
    return Date.UTC(this.year, this.month - 1, this.day);
  }

  public toJSON(): string {
    return this.toString();
  }

  public toISOFormat(): string {
    return datefns.format(this.thisToDate(), ISO_FORMAT);
  }
}
