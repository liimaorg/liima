import { TestBed } from '@angular/core/testing';
import { Component, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { BasePropertiesComponent } from './base-properties.component';
import { Property } from '../models/property';
import { Observable, of } from 'rxjs';
import { PropertyUpdate } from '../services/resource-properties.service';
import { PropertyDeleteModalService } from '../services/property-delete-modal.service';

@Component({
  selector: 'app-test-base-properties',
  template: '',
  standalone: true,
})
class TestBasePropertiesComponent extends BasePropertiesComponent {
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

  protected bulkUpdateProperties(
    entityId: number,
    updatedProperties: PropertyUpdate[],
    resetProperties: PropertyUpdate[],
    contextId: number,
  ): Observable<void> {
    return of(void 0);
  }

  protected reloadProperties(entityId: number, contextId: number): void {
    // Mock implementation
  }

  protected getDeleteParams(): [number | undefined, number | undefined] {
    return [1, undefined];
  }

  protected getSaveDescriptorParams(): [number | undefined, number | undefined] {
    return [1, undefined];
  }
}

describe('BasePropertiesComponent', () => {
  let component: TestBasePropertiesComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestBasePropertiesComponent],
      providers: [PropertyDeleteModalService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(TestBasePropertiesComponent);
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

  it('should track property validation changes', () => {
    component.onPropertyValidationChange('testProp', true);
    expect(component.hasValidationErrors()).toBe(true);

    component.onPropertyValidationChange('testProp', false);
    expect(component.hasValidationErrors()).toBe(false);
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

  it('should not save when there are validation errors', () => {
    component.onPropertyChange('testProp', 'newValue');
    component.onPropertyValidationChange('testProp', true);

    component.saveChanges();
    expect(component.isSaving()).toBe(false);
  });

  it('should clear error and success messages on reset', () => {
    component.errorMessage.set('Error');
    component.successMessage.set('Success');

    component.resetChanges();

    expect(component.errorMessage()).toBeNull();
    expect(component.successMessage()).toBeNull();
  });
});
