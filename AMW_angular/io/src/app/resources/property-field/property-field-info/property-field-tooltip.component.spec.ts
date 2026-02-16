import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PropertyFieldTooltipComponent } from './property-field-tooltip.component';
import { Property } from '../../models/property';

describe('PropertyFieldTooltipComponent', () => {
  let component: PropertyFieldTooltipComponent;
  let fixture: ComponentFixture<PropertyFieldTooltipComponent>;

  const mockProperty: Property = {
    name: 'testProperty',
    value: 'testValue',
    replacedValue: 'oldValue',
    generalComment: 'test comment',
    valueComment: 'value comment',
    context: 'testContext',
    definedInContext: true,
    originOfValue: 'testOrigin',
    encrypted: false,
    exampleValue: 'example',
    defaultValue: 'default',
    mik: 'testMIK',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PropertyFieldTooltipComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PropertyFieldTooltipComponent);
    component = fixture.componentInstance;

    // Set default input values
    fixture.componentRef.setInput('property', mockProperty);
    fixture.componentRef.setInput('isInfo', true);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('infoText', () => {
    it('should return empty string when definedInContext and values are empty', () => {
      fixture.componentRef.setInput('property', {
        ...mockProperty,
        definedInContext: true,
        value: null,
        replacedValue: '',
        originOfValue: null,
      });
      fixture.componentRef.setInput('isInfo', true);
      fixture.detectChanges();

      expect(component.infoText()).toBe('');
    });

    it('should return encrypted message when property is encrypted', () => {
      fixture.componentRef.setInput('property', {
        ...mockProperty,
        definedInContext: true,
        encrypted: true,
      });
      fixture.componentRef.setInput('isInfo', true);
      fixture.detectChanges();

      expect(component.infoText()).toBe("Replaces value of 'testOrigin'");
    });

    it('should return replacement message when property has values', () => {
      fixture.componentRef.setInput('property', mockProperty);
      fixture.componentRef.setInput('isInfo', true);
      fixture.detectChanges();

      expect(component.infoText()).toBe("Replaces value 'oldValue' of 'testOrigin'");
    });

    it('should return defined in message when not definedInContext and no replaced value', () => {
      fixture.componentRef.setInput('property', {
        ...mockProperty,
        definedInContext: false,
        replacedValue: '',
      });
      fixture.componentRef.setInput('isInfo', true);
      fixture.detectChanges();

      expect(component.infoText()).toBe("Defined in 'testOrigin'");
    });

    it('should return empty string when not definedInContext but has replaced value', () => {
      fixture.componentRef.setInput('property', {
        ...mockProperty,
        definedInContext: false,
        replacedValue: 'someValue',
      });
      fixture.componentRef.setInput('isInfo', true);
      fixture.detectChanges();

      expect(component.infoText()).toBe('');
    });

    it('should return empty string when not definedInContext and originOfValue is empty', () => {
      fixture.componentRef.setInput('property', {
        ...mockProperty,
        definedInContext: false,
        replacedValue: '',
        originOfValue: '',
      });
      fixture.componentRef.setInput('isInfo', true);
      fixture.detectChanges();

      expect(component.infoText()).toBe('');
    });
  });

  describe('dataTable', () => {
    it('should return data table when isInfo is true', () => {
      fixture.componentRef.setInput('property', mockProperty);
      fixture.componentRef.setInput('isInfo', true);
      fixture.detectChanges();

      const result = component.dataTable();
      expect(result).toEqual([
        { label: 'TechKey', value: 'testProperty' },
        { label: 'Example value', value: 'example' },
        { label: 'Default', value: 'default' },
        { label: 'Comment', value: 'test comment' },
        { label: 'Machine Interpretation Key:', value: 'testMIK' },
      ]);
    });

    it('should return undefined when isInfo is false', () => {
      fixture.componentRef.setInput('property', mockProperty);
      fixture.componentRef.setInput('isInfo', false);
      fixture.detectChanges();

      expect(component.dataTable()).toBeUndefined();
    });
  });

  describe('helper methods', () => {
    it('isValueEmpty should return true for null values', () => {
      expect(component['isValueEmpty'](null)).toBe(true);
    });

    it('isValueEmpty should return true for empty strings', () => {
      expect(component['isValueEmpty']('')).toBe(true);
    });

    it('isValueEmpty should return false for valid strings', () => {
      expect(component['isValueEmpty']('test')).toBe(false);
    });

    it('hasReplacedValue should return true when replacedValue exists', () => {
      const prop = { ...mockProperty, replacedValue: 'someValue' };
      expect(component['hasReplacedValue'](prop)).toBe(true);
    });

    it('hasReplacedValue should return false when replacedValue is null', () => {
      const prop = { ...mockProperty, replacedValue: null };
      expect(component['hasReplacedValue'](prop)).toBe(false);
    });

    it('hasReplacedValue should return false when replacedValue is empty', () => {
      const prop = { ...mockProperty, replacedValue: '' };
      expect(component['hasReplacedValue'](prop)).toBe(false);
    });
  });
});
