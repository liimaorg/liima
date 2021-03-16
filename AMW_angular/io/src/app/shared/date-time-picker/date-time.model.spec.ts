import { TestBed } from '@angular/core/testing';
import { DateTimePickerComponent } from './date-time-picker.component';
import { DateTimeModel } from './date-time.model';
import * as datefns from 'date-fns';

describe('DateTimeModel', () => {
  it('should convert dates correctly', () => {
    var fixture = TestBed.createComponent(DateTimePickerComponent);
    var component = fixture.componentInstance;
    var testDate = '02.01.2017 12:00';

    var m = datefns.parse(testDate, component.dateStringFormat, new Date());
    var dateTimeFromString = DateTimeModel.fromLocalString(testDate, component.dateStringFormat);
    var dateTimeFromEpoch = DateTimeModel.fromEpoch(m.valueOf());

    expect(dateTimeFromString.day).toEqual(m.getDay());
    expect(dateTimeFromString.month).toEqual(m.getMonth() + 1);

    expect(dateTimeFromString).toEqual(dateTimeFromEpoch);

    expect(dateTimeFromString.toEpoch()).toEqual(m.valueOf());
  });
});
