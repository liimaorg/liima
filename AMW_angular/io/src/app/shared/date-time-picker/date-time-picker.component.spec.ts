import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, NgControl } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { DateTimePickerComponent } from './date-time-picker.component';
import { DateTimeModel } from './date-time.model';
import { IconComponent } from '../icon/icon.component';
import { ButtonComponent } from '../button/button.component';

describe('DateTimePickerComponent', () => {
  let component: DateTimePickerComponent;
  let fixture: ComponentFixture<DateTimePickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DateTimePickerComponent, FormsModule, NgbModule, IconComponent, ButtonComponent],
      providers: [
        {
          provide: NgControl,
          useValue: { valid: true },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DateTimePickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('writeValue', () => {
    it('should initialize datetime with provided model values', () => {
      const model = new DateTimeModel({
        year: 2025,
        month: 12,
        day: 31,
        hour: 23,
        minute: 59,
        second: 0,
      });

      component.writeValue(model);

      expect(component.datetime.year).toBe(2025);
      expect(component.datetime.month).toBe(12);
      expect(component.datetime.day).toBe(31);
      expect(component.datetime.hour).toBe(23);
      expect(component.datetime.minute).toBe(59);
      expect(component.datetime.second).toBe(0);
    });

    it('should create a fresh DateTimeModel instance on writeValue', () => {
      const model1 = new DateTimeModel({
        year: 2025,
        month: 1,
        day: 1,
      });

      component.writeValue(model1);
      const firstInstance = component.datetime;

      const model2 = new DateTimeModel({
        year: 2025,
        month: 12,
        day: 31,
      });

      component.writeValue(model2);
      const secondInstance = component.datetime;

      // Ensure fresh instance is created each time (not mutating existing object)
      expect(firstInstance).not.toBe(secondInstance);
      expect(secondInstance.month).toBe(12);
      expect(secondInstance.day).toBe(31);
    });

    it('should reset datetime to new instance when null is provided', () => {
      component.writeValue(
        new DateTimeModel({
          year: 2025,
          month: 12,
          day: 31,
        }),
      );

      component.writeValue(null);

      expect(component.datetime.year).toBeUndefined();
      expect(component.datetime.month).toBeUndefined();
      expect(component.datetime.day).toBeUndefined();
    });

    it('should set dateString after writeValue', () => {
      const model = new DateTimeModel({
        year: 2025,
        month: 12,
        day: 31,
        hour: 12,
        minute: 0,
        second: 0,
      });

      component.writeValue(model);

      expect(component.dateString).toBeTruthy();
      expect(component.dateString).toContain('31');
      expect(component.dateString).toContain('12');
      expect(component.dateString).toContain('2025');
    });
  });

  describe('onDateChange', () => {
    it('should update datetime with selected date', () => {
      component.writeValue(
        new DateTimeModel({
          year: 2025,
          month: 1,
          day: 1,
          hour: 10,
          minute: 30,
        }),
      );

      component.onDateChange({ year: 2025, month: 12, day: 25 });

      expect(component.datetime.year).toBe(2025);
      expect(component.datetime.month).toBe(12);
      expect(component.datetime.day).toBe(25);
      // Time should remain unchanged
      expect(component.datetime.hour).toBe(10);
      expect(component.datetime.minute).toBe(30);
    });

    it('should call onChange when date changes', () => {
      const onChange = vi.spyOn(component, 'onChange');

      component.onDateChange({ year: 2025, month: 12, day: 25 });

      expect(onChange).toHaveBeenCalled();
    });
  });

  describe('onTimeChange', () => {
    it('should update datetime with selected time', () => {
      component.writeValue(
        new DateTimeModel({
          year: 2025,
          month: 12,
          day: 25,
          hour: 10,
          minute: 30,
        }),
      );

      component.onTimeChange({ hour: 23, minute: 59, second: 0 });

      expect(component.datetime.hour).toBe(23);
      expect(component.datetime.minute).toBe(59);
      expect(component.datetime.second).toBe(0);
      // Date should remain unchanged
      expect(component.datetime.year).toBe(2025);
      expect(component.datetime.month).toBe(12);
      expect(component.datetime.day).toBe(25);
    });

    it('should call onChange when time changes', () => {
      const onChange = vi.spyOn(component, 'onChange');

      component.onTimeChange({ hour: 23, minute: 59, second: 0 });

      expect(onChange).toHaveBeenCalled();
    });
  });
});
