import { TestBed } from '@angular/core/testing';
import { DatePickerComponent } from './date-picker.component';
import { DateModel } from './date.model';
import * as datefns from 'date-fns';

describe('DateModel', () => {
  it('should convert dates correctly', () => {
    const fixture = TestBed.createComponent(DatePickerComponent);
    const component = fixture.componentInstance;
    const testDate = '02.01.2017'; // 1483315200000
    const m = new Date(Date.UTC(2017, 1 - 1, 2)); // Date in UTC

    const dateTimeFromString = DateModel.fromLocalString(testDate, component.dateStringFormat);
    const dateTimeFromEpoch = DateModel.fromEpoch(m.getTime());

    expect(dateTimeFromString.day).toEqual(m.getDate());
    expect(dateTimeFromString.month).toEqual(m.getMonth() + 1);

    expect(dateTimeFromString).toEqual(dateTimeFromEpoch);

    expect(dateTimeFromString.toEpoch()).toEqual(m.getTime());
  });

  it('should return the same epoch after fromEpoch and toEpoch', () => {
    const epoch = 1754006400000 ; // 1 August 2025 00:00:00 GMT
    const model = DateModel.fromEpoch(epoch);

    expect(model.year).toEqual(2025);
    expect(model.month).toEqual(8);
    expect(model.day).toEqual(1);

    expect(model.toEpoch()).toEqual(epoch);
  });
});
