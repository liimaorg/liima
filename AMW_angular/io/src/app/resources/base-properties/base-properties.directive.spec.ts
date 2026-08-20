import { TestBed } from '@angular/core/testing';
import { Component, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { BasePropertiesDirective } from './base-properties.directive';
import { Property } from '../models/property';
import { Observable, of } from 'rxjs';
import { PropertyUpdate } from '../services/resource-properties.service';
import { PropertyDeleteModalService } from '../services/property-delete-modal.service';

@Component({
  selector: 'app-test-base-properties',
  template: '',
  standalone: true,
})
class TestBasePropertiesDirective extends BasePropertiesDirective {
  protected afterPropertiesSaved(): void {
    // Mock implementation
  }
  properties = signal<Property[]>([]);
  permissions = signal({ canUpdateProperty: true, canDecryptProperties: true });
  isLoading = signal(false);

  protected getEntityId(): number {
    return 1;
  }

  protected getUnsavedChangesKey(): string {
    return 'test-key';
  }

  protected getEditorOptions() {
    return { includeResetsInHasChanges: true, unmarkResetOnChange: true };
  }
  /* eslint-disable @typescript-eslint/no-unused-vars */
  protected bulkUpdateProperties(
    _entityId: number,
    _updatedProperties: PropertyUpdate[],
    resetProperties: PropertyUpdate[],
    contextId: number,
  ): Observable<void> {
    // Mock implementation
    return of(void 0);
  }
  protected reloadProperties(entityId: number, contextId: number): void {
    // Mock implementation
  }
  /* eslint-enable @typescript-eslint/no-unused-vars */

  protected getDeleteParams(): [number | undefined, number | undefined] {
    return [1, undefined];
  }

  protected getSaveDescriptorParams(): [number | undefined, number | undefined] {
    return [1, undefined];
  }
}

describe('BasePropertiesDirective', () => {
  let component: TestBasePropertiesDirective;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestBasePropertiesDirective],
      providers: [PropertyDeleteModalService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(TestBasePropertiesDirective);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('contextId', 1);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should track property changes', () => {
    const testProperty: Property = {
      name: 'testProp',
      value: 'oldValue',
      disabled: false,
    } as Property;
    component.properties.set([testProperty]);

    component.onPropertyChange('testProp', 'newValue');
    expect(component.hasChanges()).toBe(true);
  });

  it('should not treat an empty value as a change from null', () => {
    const testProperty: Property = {
      name: 'testProp',
      value: null,
      disabled: false,
    } as Property;
    component.properties.set([testProperty]);

    component.onPropertyChange('testProp', '');

    expect(component.hasChanges()).toBe(false);
  });

  it('should reset changes', () => {
    const testProperty: Property = {
      name: 'testProp',
      value: 'oldValue',
      disabled: false,
    } as Property;
    component.properties.set([testProperty]);

    component.onPropertyChange('testProp', 'newValue');
    expect(component.hasChanges()).toBe(true);

    component.resetChanges();
    expect(component.hasChanges()).toBe(false);
  });

  it('should clear error message on reset', () => {
    component.errorMessage.set('Error');

    component.resetChanges();

    expect(component.errorMessage()).toBeNull();
  });
});
