import { TestBed } from '@angular/core/testing';
import { DatePickerComponent } from './date-picker.component';
import { DateModel } from './date.model';
import * as datefns from 'date-fns';

describe('DateModel', () => {
  it('should convert dates correctly', () => {
    const fixture = TestBed.createComponent(DatePickerComponent);
    const component = fixture.componentInstance;
    const testDate = '02.01.2017';

    const m = datefns.parse(testDate, component.dateStringFormat, new Date());
    const dateTimeFromString = DateModel.fromLocalString(testDate, component.dateStringFormat);
    const dateTimeFromEpoch = DateModel.fromEpoch(m.getTime());

    expect(dateTimeFromString.day).toEqual(m.getDate());
    expect(dateTimeFromString.month).toEqual(m.getMonth() + 1);

    expect(dateTimeFromString).toEqual(dateTimeFromEpoch);

    expect(dateTimeFromString.toEpoch()).toEqual(m.getTime());
  });
});
