import { TestBed } from '@angular/core/testing';
import { DateTimePickerComponent } from './date-time-picker.component';
import { DateTimeModel } from './date-time.model';
import * as datefns from 'date-fns';

describe('DateTimeModel', () => {
  it('should convert dates correctly', () => {
    const fixture = TestBed.createComponent(DateTimePickerComponent);
    const component = fixture.componentInstance;
    const testDate = '02.01.2017 10:00';

    const m = datefns.parse(testDate, component.dateStringFormat, new Date());
    const dateTimeFromString = DateTimeModel.fromLocalString(testDate, component.dateStringFormat);
    const dateTimeFromEpoch = DateTimeModel.fromEpoch(m.getTime());

    expect(dateTimeFromString.day).toEqual(m.getDate());
    expect(dateTimeFromString.month).toEqual(m.getMonth() + 1);

    expect(dateTimeFromString).toEqual(dateTimeFromEpoch);

    expect(dateTimeFromString.toEpoch()).toEqual(m.getTime());
  });
});
