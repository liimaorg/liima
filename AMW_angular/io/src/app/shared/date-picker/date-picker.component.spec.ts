import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, NgControl } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { DatePickerComponent } from './date-picker.component';
import { DateModel } from './date.model';
import { IconComponent } from '../icon/icon.component';
import { ButtonComponent } from '../button/button.component';

describe('DatePickerComponent', () => {
  let component: DatePickerComponent;
  let fixture: ComponentFixture<DatePickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DatePickerComponent, FormsModule, NgbModule, IconComponent, ButtonComponent],
      providers: [
        {
          provide: NgControl,
          useValue: { valid: true },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DatePickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('writeValue', () => {
    it('should initialize date with provided model values', () => {
      const model = new DateModel({
        year: 2025,
        month: 12,
        day: 31,
      });

      component.writeValue(model);

      expect(component.date.year).toBe(2025);
      expect(component.date.month).toBe(12);
      expect(component.date.day).toBe(31);
    });

    it('should create a fresh DateModel instance on writeValue', () => {
      const model1 = new DateModel({
        year: 2025,
        month: 1,
        day: 1,
      });

      component.writeValue(model1);
      const firstInstance = component.date;

      const model2 = new DateModel({
        year: 2025,
        month: 12,
        day: 31,
      });

      component.writeValue(model2);
      const secondInstance = component.date;

      // Ensure fresh instance is created each time (not mutating existing object)
      expect(firstInstance).not.toBe(secondInstance);
      expect(secondInstance.month).toBe(12);
      expect(secondInstance.day).toBe(31);
    });

    it('should reset date to new instance when null is provided', () => {
      component.writeValue(
        new DateModel({
          year: 2025,
          month: 12,
          day: 31,
        }),
      );

      component.writeValue(null);

      expect(component.date.year).toBeUndefined();
      expect(component.date.month).toBeUndefined();
      expect(component.date.day).toBeUndefined();
    });

    it('should set dateString after writeValue', () => {
      const model = new DateModel({
        year: 2025,
        month: 12,
        day: 31,
      });

      component.writeValue(model);

      expect(component.dateString).toBeTruthy();
      expect(component.dateString).toContain('31');
      expect(component.dateString).toContain('12');
      expect(component.dateString).toContain('2025');
    });
  });

  describe('onDateChange', () => {
    it('should update date with selected date', () => {
      component.writeValue(
        new DateModel({
          year: 2025,
          month: 1,
          day: 1,
        }),
      );

      component.onDateChange({ year: 2025, month: 12, day: 25 });

      expect(component.date.year).toBe(2025);
      expect(component.date.month).toBe(12);
      expect(component.date.day).toBe(25);
    });

    it('should call onChange when date changes', () => {
      const onChange = vi.spyOn(component, 'onChange');

      component.onDateChange({ year: 2025, month: 12, day: 25 });

      expect(onChange).toHaveBeenCalled();
    });

    it('should update dateString after date change', () => {
      component.writeValue(new DateModel({ year: 2025, month: 1, day: 1 }));

      component.onDateChange({ year: 2025, month: 12, day: 25 });

      expect(component.dateString).toContain('25');
      expect(component.dateString).toContain('12');
    });
  });

  describe('onDateStringChange', () => {
    it('should parse valid date string and update model', () => {
      const onChange = vi.spyOn(component, 'onChange');
      const event = {
        target: { value: '31.12.2025' },
      };

      component.onDateStringChange(event);

      expect(component.date.year).toBe(2025);
      expect(component.date.month).toBe(12);
      expect(component.date.day).toBe(31);
      expect(onChange).toHaveBeenCalledWith(expect.any(DateModel));
      expect(component.errorMessage).toBe('');
    });

    it('should clear date and dateString when empty string is provided', () => {
      component.writeValue(
        new DateModel({
          year: 2025,
          month: 12,
          day: 31,
        }),
      );

      const onChange = vi.spyOn(component, 'onChange');
      const event = {
        target: { value: '' },
      };

      component.onDateStringChange(event);

      expect(component.date.year).toBeUndefined();
      expect(component.date.month).toBeUndefined();
      expect(component.date.day).toBeUndefined();
      expect(component.dateString).toBe('');
      expect(onChange).toHaveBeenCalled();
    });

    it('should set error message for invalid date string', () => {
      const event = {
        target: { value: 'invalid-date' },
      };

      component.onDateStringChange(event);

      expect(component.errorMessage).toBe('Invalid date!');
    });
  });

  describe('setDateString', () => {
    it('should format and set dateString', () => {
      component.date = new DateModel({ year: 2025, month: 12, day: 31 });
      const onChange = vi.spyOn(component, 'onChange');

      component.setDateString();

      expect(component.dateString).toBeTruthy();
      expect(component.dateString).toContain('31');
      expect(component.dateString).toContain('12');
      expect(component.dateString).toContain('2025');
      expect(onChange).toHaveBeenCalledWith(expect.any(DateModel));
    });
  });

  describe('cancel', () => {
    it('should clear error message', () => {
      component.errorMessage = 'Some error';

      component.cancel();

      expect(component.errorMessage).toBe('');
    });
  });

  describe('inputBlur', () => {
    it('should call onTouched', () => {
      const onTouched = vi.spyOn(component, 'onTouched');

      component.inputBlur();

      expect(onTouched).toHaveBeenCalled();
    });
  });
});
