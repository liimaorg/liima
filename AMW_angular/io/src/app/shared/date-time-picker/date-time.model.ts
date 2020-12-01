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

  private static fromMoment(m: moment.Moment): DateTimeModel {
    if (!m.isValid()) {
      return null;
    }
    return new DateTimeModel({
      year: m.year(),
      // months start at 0
      month: m.month() + 1,
      // date is the day of month
      day: m.date(),
      hour: m.hour(),
      minute: m.minute(),
      second: m.second(),
      timeZoneOffset: m.utcOffset(),
    });
  }

  public static fromLocalString(dateString: string, format: string): DateTimeModel {
    const m = moment(dateString, format);
    return this.fromMoment(m);
  }

  public static fromEpoch(epoch: number) {
    const m = moment(epoch);
    return this.fromMoment(m);
  }

  private toMoment(): moment.Moment {
    return moment({ year: this.year, month: this.month - 1, date: this.day, hour: this.hour, minute: this.minute, second: this.second});;
  }

  public toString(format: string): string {
    const m = this.toMoment();
    if (!m.isValid()) {
      return null;
    }
    return m.format(format);
  }

  public toEpoch(): number {
    const m = this.toMoment();
    if (!m.isValid()) {
      return null;
    }
    return m.valueOf();
  }
}
