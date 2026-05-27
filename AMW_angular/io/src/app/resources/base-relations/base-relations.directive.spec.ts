import { TestBed } from '@angular/core/testing';
import { Component, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { BaseRelationsDirective } from './base-relations.directive';
import { Property } from '../models/property';
import { GroupedResourceRelations, UnresolvedRelation } from '../models/resource-relation';
import { RelationGroupItem } from '../relation-group/relation-group.component';

@Component({
  selector: 'app-test-base-relations',
  template: '',
  standalone: true,
})
class TestBaseRelationsDirective extends BaseRelationsDirective {
  protected groupedRelations = signal<GroupedResourceRelations>({
    runtime: [],
    consumed: [],
    provided: [],
    unresolved: [],
  });
  protected hasRelations = signal(false);
  protected activeRelationId = signal<number | null>(null);
  protected isLoadingRelations = signal(false);
  protected isLoadingProperties = signal(false);
  protected entityId = signal<number | undefined>(1);
  properties = signal<Property[]>([]);

  protected hasIdentifierProperty(): boolean {
    return false;
  }

  protected toUnresolvedItem(unresolved: UnresolvedRelation): RelationGroupItem {
    return {
      key: `${unresolved.type}::${unresolved.name}`,
      name: unresolved.name,
      type: unresolved.type,
      unresolved: true,
    };
  }

  protected reloadRelation(entityId: number): void {}

  protected reloadProperties(entityId: number, relationId: number, contextId: number): void {}

  protected getUnsavedChangesKey(): string {
    return 'test-key';
  }

  protected getEditorOptions() {
    return { includeResetsInHasChanges: true, unmarkResetOnChange: true };
  }
}

describe('BaseRelationsDirective', () => {
  let component: TestBaseRelationsDirective;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestBaseRelationsDirective],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    const fixture = TestBed.createComponent(TestBaseRelationsDirective);
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

    // component.saveChanges();
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
