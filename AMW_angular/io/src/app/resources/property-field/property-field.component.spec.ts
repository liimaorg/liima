import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ComponentRef } from '@angular/core';
import { vi } from 'vitest';
import { PropertyFieldComponent } from './property-field.component';
import { Property } from '../models/property';

describe('PropertyFieldComponent', () => {
  let fixture: ComponentFixture<PropertyFieldComponent>;
  let component: PropertyFieldComponent;
  let componentRef: ComponentRef<PropertyFieldComponent>;

  const baseProperty = (overrides: Partial<Property> = {}): Property =>
    ({
      name: 'prop',
      value: '',
      replacedValue: '',
      generalComment: '',
      valueComment: '',
      context: 'Global',
      definedInContext: false,
      nullable: true,
      optional: true,
      encrypted: false,
      validationRegex: '.*',
      defaultValue: '',
      exampleValue: '',
      mik: '',
      ...overrides,
    }) as Property;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PropertyFieldComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PropertyFieldComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;

    componentRef.setInput('mode', 'resource');
  });

  it('should create', () => {
    componentRef.setInput('property', baseProperty());
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should show required error when required and value+default+mik are empty', () => {
    componentRef.setInput(
      'property',
      baseProperty({ nullable: false, optional: false, defaultValue: '', mik: '', validationRegex: '.*' }),
    );
    fixture.detectChanges();

    component.localValue = '';
    component.onBlur();

    expect(component.validationError()).toBe('This field is required');
  });

  it('should not show required error when required but mik is present (no explicit value/default)', () => {
    componentRef.setInput(
      'property',
      baseProperty({ nullable: false, optional: false, defaultValue: '', mik: 'SOME_MIK', validationRegex: '.*' }),
    );
    fixture.detectChanges();

    component.localValue = '';
    component.onBlur();

    expect(component.validationError()).toBeNull();
  });

  it('should skip regex validation when optional/nullable and mik is present while value+default are empty', () => {
    componentRef.setInput(
      'property',
      baseProperty({ nullable: true, optional: true, defaultValue: '', mik: 'SOME_MIK', validationRegex: '[0-9]+' }),
    );
    fixture.detectChanges();

    component.localValue = '';
    component.onBlur();

    expect(component.validationError()).toBeNull();
  });

  it('should validate regex against defaultValue when value is empty', () => {
    componentRef.setInput(
      'property',
      baseProperty({ nullable: true, optional: true, defaultValue: 'abc', mik: '', validationRegex: '[a-z]+' }),
    );
    fixture.detectChanges();

    component.localValue = '';
    component.onBlur();

    expect(component.validationError()).toBeNull();
  });

  it('should enforce full match semantics for regex', () => {
    componentRef.setInput(
      'property',
      baseProperty({ nullable: true, optional: true, defaultValue: '', mik: '', validationRegex: '[a-z]+' }),
    );
    fixture.detectChanges();

    component.localValue = 'abc123';
    component.onBlur();

    expect(component.validationError()).toBe('Value does not match the required pattern');
  });

  it('should treat invalid regex as an error', () => {
    componentRef.setInput(
      'property',
      baseProperty({ nullable: true, optional: true, defaultValue: '', mik: '', validationRegex: '(' }),
    );
    fixture.detectChanges();

    component.localValue = 'anything';
    component.onBlur();

    expect(component.validationError()).toBe('Value does not match the required pattern');
  });

  it('should prefer defaultValue over exampleValue for placeholder', () => {
    componentRef.setInput(
      'property',
      baseProperty({ defaultValue: 'DEFAULT', exampleValue: 'EXAMPLE', nullable: true, optional: true }),
    );
    fixture.detectChanges();

    const input: HTMLInputElement | null = fixture.nativeElement.querySelector('input');
    expect(input).not.toBeNull();
    expect(input?.getAttribute('placeholder')).toBe('DEFAULT');
  });

  it('should not render reset checkbox when property is not defined in current context', () => {
    componentRef.setInput('property', baseProperty({ definedInContext: false, value: 'X', replacedValue: '' }));
    fixture.detectChanges();

    const reset = fixture.nativeElement.querySelector(`input[type="checkbox"]`);
    expect(reset).toBeNull();
  });

  it('should render reset checkbox when property is defined in current context even if value is null', () => {
    componentRef.setInput(
      'property',
      baseProperty({ name: 'pNull', definedInContext: true, value: null, replacedValue: '' }),
    );
    fixture.detectChanges();

    const reset: HTMLInputElement | null = fixture.nativeElement.querySelector(`#reset-pNull`);
    expect(reset).not.toBeNull();
  });

  it('should render reset checkbox when replacedValue exists and toggle value + emit resetChange', () => {
    componentRef.setInput(
      'property',
      baseProperty({
        name: 'p1',
        value: 'CURRENT',
        replacedValue: 'PARENT',
        definedInContext: true,
        nullable: true,
        optional: true,
      }),
    );
    fixture.detectChanges();

    const resetSpy = vi.fn();
    component.resetChange.subscribe(resetSpy);

    const reset: HTMLInputElement | null = fixture.nativeElement.querySelector(`#reset-p1`);
    expect(reset).not.toBeNull();

    // check reset => value becomes replacedValue and input is disabled
    reset!.checked = true;
    const resetOnEvent = new Event('change');
    Object.defineProperty(resetOnEvent, 'target', { value: reset });
    (component as PropertyFieldComponent).toggleReset(resetOnEvent);
    fixture.detectChanges();

    expect(component.localValue).toBe('PARENT');
    expect(component.resetChecked()).toBe(true);
    expect(component.isInputDisabled()).toBe(true);
    expect(resetSpy).toHaveBeenCalledWith(true);

    // uncheck reset => value becomes original and input enabled again (if not otherwise disabled)
    reset!.checked = false;
    const resetOffEvent = new Event('change');
    Object.defineProperty(resetOffEvent, 'target', { value: reset });
    (component as PropertyFieldComponent).toggleReset(resetOffEvent);
    fixture.detectChanges();

    expect(component.localValue).toBe('CURRENT');
    expect(component.resetChecked()).toBe(false);
    expect(component.isInputDisabled()).toBe(false);
    expect(resetSpy).toHaveBeenCalledWith(false);
  });
});
