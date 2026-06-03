import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { Component, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { BaseRelationsDirective } from './base-relations.directive';
import { Property } from '../models/property';
import { GroupedResourceRelations, UnresolvedRelation } from '../models/resource-relation';
import { RelationGroupItem } from '../relation-group/relation-group.component';
import { Observable } from 'rxjs';
import { PropertyUpdate } from '../services/resource-properties.service';

@Component({
  selector: 'app-test-base-relations',
  template: '',
  standalone: true,
})
class TestBaseRelationsDirective extends BaseRelationsDirective {
  protected bulkUpdateProperties(
    entityId: number,
    updatedProperties: PropertyUpdate[],
    resetProperties: PropertyUpdate[],
    contextId: number,
  ): Observable<void> {
    throw new Error('Method not implemented.');
  }
  protected afterPropertiesSaved(): void {
    throw new Error('Method not implemented.');
  }
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

  it('should clear invalid properties on reset', () => {
    component.onPropertyValidationChange('testProp', true);
    expect(component.hasValidationErrors()).toBe(true);

    component.resetChanges();

    expect(component.hasValidationErrors()).toBe(false);
  });

  it('should track property resets as changes', () => {
    component.onPropertyReset('testProp', true);
    expect(component.hasChanges()).toBe(true);
  });

  it('should untrack property reset when unchecked', () => {
    component.onPropertyReset('testProp', true);
    component.onPropertyReset('testProp', false);
    expect(component.hasChanges()).toBe(false);
  });

  it('should unmark reset when property value changes', () => {
    const testProperty: Property = {
      name: 'testProp',
      value: 'oldValue',
      disabled: false,
    } as Property;
    component.properties.set([testProperty]);

    component.onPropertyReset('testProp', true);
    expect(component.hasChanges()).toBe(true);

    component.onPropertyChange('testProp', 'newValue');

    const resets = (component as any).editor.resetProperties();
    expect(resets.has('testProp')).toBe(false);
  });

  it('should return active relation id from getRelationId', () => {
    component['activeRelationId'].set(42);
    expect(component.getRelationId()).toBe(42);
  });

  it('should return null from getRelationId when no relation selected', () => {
    component['activeRelationId'].set(null);
    expect(component.getRelationId()).toBeNull();
  });

  it('should set active relation id and navigate on onItemSelected with numeric key', () => {
    const spy = vi.spyOn(component, 'setQueryParamForRelationId').mockImplementation(() => {});
    component.onItemSelected({ key: 7, name: 'rel', type: 'consumed' });
    expect(component['activeRelationId']()).toBe(7);
    expect(spy).toHaveBeenCalledWith(7);
  });

  it('should ignore onItemSelected when key is not a number', () => {
    const spy = vi.spyOn(component, 'setQueryParamForRelationId').mockImplementation(() => {});
    component.onItemSelected({ key: 'unresolved::foo', name: 'foo', type: 'consumed', unresolved: true });
    expect(spy).not.toHaveBeenCalled();
  });

  it('should map ResourceRelation to RelationGroupItem via toItem', () => {
    const relation = {
      id: 5,
      relatedResourceName: 'MyApp',
      type: 'consumed',
      relatedResourceRelease: '1.0',
      identifier: 'myapp',
      slaveId: 10,
      relationName: 'rel',
      relationType: 'consumed' as const,
    };
    const item = component.toItem(relation);
    expect(item).toEqual({
      key: 5,
      name: 'MyApp',
      type: 'consumed',
      release: '1.0',
      identifier: 'myapp',
    });
  });

  it('should compute runtimeItems from groupedRelations', () => {
    const relation = {
      id: 1,
      relatedResourceName: 'Runtime',
      type: 'runtime',
      relatedResourceRelease: '2.0',
      slaveId: 2,
      relationName: 'rt',
      relationType: 'consumed' as const,
    };
    component['groupedRelations'].set({ runtime: [relation], consumed: [], provided: [], unresolved: [] });
    expect(component.runtimeItems().length).toBe(1);
    expect(component.runtimeItems()[0].key).toBe(1);
  });

  it('should compute consumedItems from groupedRelations', () => {
    const relation = {
      id: 2,
      relatedResourceName: 'DB',
      type: 'consumed',
      relatedResourceRelease: '3.0',
      slaveId: 3,
      relationName: 'db',
      relationType: 'consumed' as const,
    };
    component['groupedRelations'].set({ runtime: [], consumed: [relation], provided: [], unresolved: [] });
    expect(component.consumedItems().length).toBe(1);
    expect(component.consumedItems()[0].name).toBe('DB');
  });

  it('should compute providedItems from groupedRelations', () => {
    const relation = {
      id: 3,
      relatedResourceName: 'API',
      type: 'provided',
      relatedResourceRelease: '1.5',
      slaveId: 4,
      relationName: 'api',
      relationType: 'provided' as const,
    };
    component['groupedRelations'].set({ runtime: [], consumed: [], provided: [relation], unresolved: [] });
    expect(component.providedItems().length).toBe(1);
    expect(component.providedItems()[0].name).toBe('API');
  });

  it('should compute unresolvedItems from groupedRelations', () => {
    const unresolved: UnresolvedRelation = { type: 'consumed', name: 'MissingApp' };
    component['groupedRelations'].set({ runtime: [], consumed: [], provided: [], unresolved: [unresolved] });
    expect(component.unresolvedItems().length).toBe(1);
    expect(component.unresolvedItems()[0].key).toBe('consumed::MissingApp');
    expect(component.unresolvedItems()[0].unresolved).toBe(true);
  });

  it('should not save when there are no changes', () => {
    const spy = vi.spyOn(component as any, 'bulkUpdateProperties');
    component.saveChanges();
    expect(spy).not.toHaveBeenCalled();
    expect(component.isSaving()).toBe(false);
  });
});
