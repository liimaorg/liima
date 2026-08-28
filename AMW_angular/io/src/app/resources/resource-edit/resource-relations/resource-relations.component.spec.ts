import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ResourceRelationsComponent } from './resource-relations.component';
import { ResourceService } from '../../services/resource.service';
import { ResourceRelationsService } from '../../services/resource-relations.service';
import { ResourceActivationService } from '../../services/resource-activation.service';
import { ResourceTypesService } from '../../services/resource-types.service';
import { AuthService } from '../../../auth/auth.service';
import { EnvironmentService } from '../../../deployment/environment.service';
import { UnsavedPropertyChangesService } from '../../services/unsaved-property-changes.service';
import { ToastService } from '../../../shared/elements/toast/toast.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ActivatedRoute } from '@angular/router';
import { Resource } from '../../models/resource';
import { Release } from '../../models/release';

describe('ResourceRelationsComponent', () => {
  let component: ResourceRelationsComponent;
  let fixture: ComponentFixture<ResourceRelationsComponent>;
  let resourceSignal: ReturnType<typeof signal<Resource | null>>;
  let releasesSignal: ReturnType<typeof signal<Release[]>>;
  let groupedRelationsSignal: ReturnType<typeof signal<any>>;
  let addResourceRelation: ReturnType<typeof vi.fn>;

  const currentResource = (release: string): Resource =>
    ({ id: 1, name: 'current', type: 'APPLICATION', version: '1', release } as Resource);

  const release = (name: string, installationInProductionAt: number): Release => ({
    id: installationInProductionAt,
    release: name,
    installationInProductionAt,
  });

  beforeEach(async () => {
    resourceSignal = signal<Resource | null>(null);
    releasesSignal = signal<Release[]>([]);
    groupedRelationsSignal = signal({ runtime: [], consumed: [], provided: [], unresolved: [] });
    addResourceRelation = vi.fn(() => of(void 0));

    await TestBed.configureTestingModule({
      imports: [ResourceRelationsComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { queryParams: of({}), queryParamMap: of(new Map()) } },
        {
          provide: ResourceService,
          useValue: { resource: resourceSignal, releasesForResourceGroup: releasesSignal },
        },
        {
          provide: ResourceRelationsService,
          useValue: {
            relations: groupedRelationsSignal,
            relationProperties: signal([]),
            isLoadingRelations: signal(false),
            isLoadingRelationProperties: signal(false),
            addResourceRelation,
            setIdForResourceRelations: vi.fn(),
            setIdsForRelationProperties: vi.fn(),
          },
        },
        { provide: ResourceActivationService, useValue: { activations: signal([]) } },
        { provide: ResourceTypesService, useValue: {} },
        { provide: AuthService, useValue: { restrictions: signal([]), hasPermission: vi.fn(() => false) } },
        { provide: EnvironmentService, useValue: { environmentTree: signal([]), findEnvironmentById: vi.fn() } },
        { provide: UnsavedPropertyChangesService, useValue: { setDirty: vi.fn(), discardChangesToken: vi.fn() } },
        { provide: ToastService, useValue: { error: vi.fn(), success: vi.fn() } },
        { provide: NgbModal, useValue: { open: vi.fn(), dismissAll: vi.fn() } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceRelationsComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('contextId', 1);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load resource types for default resource type (APPLICATION)', () => {
    resourceSignal.set(currentResource('current'));
    const spy = vi.spyOn(component, 'loadAllResourceTypes' as any).mockImplementation(() => { });

    component.showAddRelationModal();

    expect(spy).toHaveBeenCalled();
  });

  it('should load resource types for default resource type (APPLICATIONSERVER)', () => {
    resourceSignal.set({ ...currentResource('current'), type: 'APPLICATIONSERVER' });
    const spy = vi.spyOn(component, 'loadAllResourceTypes' as any).mockImplementation(() => { });

    component.showAddRelationModal();

    expect(spy).toHaveBeenCalled();
  });

  it('isNode should return true for NODE resource type', () => {
    resourceSignal.set({ ...currentResource('current'), type: 'NODE' });

    expect(component['isNode']()).toBe(true);
  });

  it('isNode should return true for quoted NODE resource type', () => {
    resourceSignal.set({ ...currentResource('current'), type: '"NODE"' });

    expect(component['isNode']()).toBe(true);
  });

  it('isNode should return false for non-NODE resource type', () => {
    resourceSignal.set(currentResource('current'));

    expect(component['isNode']()).toBe(false);
  });

  it('should load resource types for default resource type (RUNTIME)', () => {
    resourceSignal.set({ ...currentResource('current'), type: 'RUNTIME' });
    const spy = vi.spyOn(component, 'loadAllResourceTypes' as any).mockImplementation(() => { });

    component.showAddRelationModal();

    expect(spy).toHaveBeenCalled();
  });

  it('should load related resource types for non-default resource type', () => {
    resourceSignal.set({ ...currentResource('current'), type: 'Webservice' });
    groupedRelationsSignal.set({
      runtime: [],
      consumed: [],
      provided: [],
      unresolved: [{ type: 'FeatureTeam', name: 'Team A' }],
    });
    const spy = vi.spyOn(component, 'loadRelatedResourceTypes' as any).mockImplementation(() => { });
    component.showAddRelationModal();
    expect(spy).toHaveBeenCalled();
  });

  it('should handle quoted type names (e.g., "APPLICATION")', () => {
    resourceSignal.set({ ...currentResource('current'), type: '"APPLICATION"' });
    const spy = vi.spyOn(component, 'loadAllResourceTypes' as any).mockImplementation(() => { });
    component.showAddRelationModal();
    expect(spy).toHaveBeenCalled();
  });

  describe('getCleanType', () => {
    it('should return the same string when no quotes are present', () => {
      expect(component['getCleanType']('NODE')).toBe('NODE');
    });

    it('should remove quotes from quoted string', () => {
      expect(component['getCleanType']('"NODE"')).toBe('NODE');
    });

    it('should return undefined when input is undefined', () => {
      expect(component['getCleanType'](undefined)).toBeUndefined();
    });

    it('should handle empty string', () => {
      expect(component['getCleanType']('')).toBe('');
    });
  });

  it('reports a newer release when its date is later than the selected release', () => {
    resourceSignal.set(currentResource('current'));
    releasesSignal.set([release('current', 100), release('newer-name-is-irrelevant', 200)]);

    expect(component.hasNewerRelease()).toBe(true);
  });

  it('does not report a newer release when the selected release has the latest date', () => {
    resourceSignal.set(currentResource('latest'));
    releasesSignal.set([release('latest', 200), release('older-name-is-irrelevant', 100)]);

    expect(component.hasNewerRelease()).toBe(false);
  });

  it('shows inline confirmation before adding when every candidate release is newer', () => {
    resourceSignal.set(currentResource('current'));
    releasesSignal.set([release('current', 100)]);
    component.availableResourceGroups.set([
      { id: 2, releases: [release('candidate-1', 200), release('candidate-2', 300)] } as Resource,
    ]);
    component.selectedResourceGroupId.set(2);
    component.addRelation();
    expect(component.requiresRelationConfirmation()).toBe(true);

    component.addRelation();
    expect(addResourceRelation).toHaveBeenCalledWith(1, 2, false);
  });

  it('does not ask for confirmation when a candidate release is not newer', () => {
    resourceSignal.set(currentResource('current'));
    releasesSignal.set([release('current', 100)]);
    component.availableResourceGroups.set([
      { id: 2, releases: [release('candidate-1', 200), release('candidate-2', 100)] } as Resource,
    ]);
    component.selectedResourceGroupId.set(2);
    component.addRelation();

    expect(component.requiresRelationConfirmation()).toBe(false);
  });
});
