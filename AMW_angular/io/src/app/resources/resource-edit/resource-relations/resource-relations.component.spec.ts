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
import { GroupedResourceRelations } from '../../models/resource-relation';
import { ConfirmDialogService } from '../../../shared/confirm-dialog/confirm-dialog.service';

describe('ResourceRelationsComponent', () => {
  let component: ResourceRelationsComponent;
  let fixture: ComponentFixture<ResourceRelationsComponent>;
  let resourceSignal: ReturnType<typeof signal<Resource | null>>;
  let releasesSignal: ReturnType<typeof signal<Release[]>>;
  let groupedRelationsSignal: ReturnType<typeof signal<GroupedResourceRelations>>;
  let addResourceRelation: ReturnType<typeof vi.fn>;

  const currentResource = (release: string): Resource =>
    ({ id: 1, name: 'current', type: 'APPLICATION', version: '1', release }) as Resource;

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
          useValue: {
            resource: resourceSignal,
            releasesForResourceGroup: releasesSignal,
            getGroupsForType: vi.fn(() => of([])),
          },
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
        {
          provide: ResourceTypesService,
          useValue: {
            getAllResourceTypes: vi.fn(() =>
              of([
                { id: 10, name: 'Webservice', hasChildren: false, children: [] },
                { id: 11, name: 'Database', hasChildren: false, children: [] },
              ]),
            ),
            getRootResourceTypes: vi.fn(() => of([])),
          },
        },
        { provide: ConfirmDialogService, useValue: { confirm: vi.fn(() => Promise.resolve(false)) } },
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

  it.each(['APPLICATION', 'APPLICATIONSERVER', 'RUNTIME', '"APPLICATION"'])(
    'keeps the tile-level Add Relation button for default resource type %s',
    (type) => {
      resourceSignal.set({ ...currentResource('current'), type });
      component['permissions'] = signal({ canUpdateProperty: true, canDecryptProperties: false });
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('.tile-action-bar')?.textContent).toContain('Add Relation');
    },
  );

  it('only offers Add Relation in the selected unresolved detail panel for non-default resources', () => {
    resourceSignal.set({ ...currentResource('current'), type: 'Custom' });
    component['permissions'] = signal({ canUpdateProperty: true, canDecryptProperties: false });
    groupedRelationsSignal.set({
      runtime: [],
      consumed: [],
      provided: [],
      unresolved: [{ resRelTypeId: 20, type: 'Webservice', name: 'primary', identifier: 'primary' }],
    });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.tile-action-bar')).toBeNull();
    expect(fixture.nativeElement.querySelector('.relation-detail-panel')).toBeNull();

    component.onItemSelected(component.unresolvedItems()[0]);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.tile-action-bar')).toBeNull();
    const addButton = fixture.nativeElement.querySelector('.relation-detail-panel app-button');
    expect(addButton.textContent).toContain('Add Relation');
    addButton.click();
    expect(TestBed.inject(NgbModal).open).toHaveBeenCalled();
    expect(component.selectedResourceTypeId()).toBe(10);
  });

  it('should load resource types for default resource type (APPLICATION)', () => {
    resourceSignal.set(currentResource('current'));
    const spy = vi.spyOn(component, 'loadAllResourceTypes' as any).mockImplementation(() => {});

    component.showAddRelationModal();

    expect(spy).toHaveBeenCalled();
  });

  it('should load resource types for default resource type (APPLICATIONSERVER)', () => {
    resourceSignal.set({ ...currentResource('current'), type: 'APPLICATIONSERVER' });
    const spy = vi.spyOn(component, 'loadAllResourceTypes' as any).mockImplementation(() => {});

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
    const spy = vi.spyOn(component, 'loadAllResourceTypes' as any).mockImplementation(() => {});

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
    const spy = vi.spyOn(component, 'loadRelatedResourceTypes' as any).mockImplementation(() => {});
    component.showAddRelationModal();
    expect(spy).toHaveBeenCalled();
  });

  it('should handle quoted type names (e.g., "APPLICATION")', () => {
    resourceSignal.set({ ...currentResource('current'), type: '"APPLICATION"' });
    const spy = vi.spyOn(component, 'loadAllResourceTypes' as any).mockImplementation(() => {});
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
    expect(addResourceRelation).toHaveBeenCalledWith(1, 2, false, undefined, undefined);
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

  it('selects the specific unresolved relation and resolves it with its name and type identifier', async () => {
    resourceSignal.set({ ...currentResource('current'), type: 'Custom' });
    const first = { resRelTypeId: 20, type: 'Webservice', name: 'primary', identifier: 'primary' };
    const second = { ...first, resRelTypeId: 21, name: 'secondary', identifier: 'secondary' };
    groupedRelationsSignal.set({ runtime: [], consumed: [], provided: [], unresolved: [first, second] });

    component.onItemSelected(component.unresolvedItems()[1]);
    fixture.detectChanges();

    expect(component.selectedUnresolvedRelation()).toEqual(second);
    expect(component.selectedRelation()).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('No resource is assigned to this relation.');
    expect(component.selectedItemKey()).toBe(component.unresolvedItems()[1].key);

    component.showAddRelationModal();
    expect(component.resourceTypes().map((type) => type.name)).toEqual(['Webservice']);
    expect(component.selectedResourceTypeId()).toBe(10);
    const modalView = component.addRelationModal.createEmbeddedView(undefined);
    modalView.detectChanges();
    await fixture.whenStable();
    const modalBody: HTMLElement = modalView.rootNodes.find(
      (node: Node) => node instanceof HTMLElement && node.classList.contains('modal-body'),
    );
    expect(modalBody.querySelector('#relationName')).toBeNull();
    expect(modalBody.textContent).not.toContain('Relation name');
    modalView.destroy();
    component.selectedResourceGroupId.set(2);
    component.addRelation();
    expect(addResourceRelation).toHaveBeenCalledWith(1, 2, false, 'secondary', 'secondary');

    groupedRelationsSignal.set({ runtime: [], consumed: [], provided: [], unresolved: [first] });
    expect(component.selectedUnresolvedRelation()).toBeNull();
    expect(component.unresolvedItems()).toHaveLength(1);
  });

  it('keeps type-defined relation names read-only without disabling ordinary relation properties', () => {
    resourceSignal.set({ ...currentResource('current'), type: 'Custom' });
    groupedRelationsSignal.set({
      runtime: [],
      consumed: [
        {
          id: 30,
          slaveId: 2,
          relatedResourceName: 'Service',
          type: 'Webservice',
          relatedResourceRelease: 'current',
          relationName: 'primary',
          identifier: 'primary',
          relationType: 'consumed',
        },
      ],
      provided: [],
      unresolved: [],
    });
    const property = { ...component.relationIdentifier(), name: 'url', descriptorId: 1, disabled: false };
    vi.spyOn(TestBed.inject(ResourceRelationsService), 'relationProperties').mockReturnValue([property]);

    expect(component.relationIdentifier().disabled).toBe(true);
    expect(component['properties']().find((item) => item.name === 'url')?.disabled).toBe(false);

    resourceSignal.set(currentResource('current'));
    expect(component.relationIdentifier().disabled).toBe(false);
    fixture.componentRef.setInput('contextId', 2);
    expect(component.relationIdentifier().disabled).toBe(true);
  });

  it.each([30, 31])('clears unresolved selection when URL navigation selects relation %s', (relationId) => {
    const unresolved = { resRelTypeId: 20, type: 'Webservice', name: 'primary' };
    groupedRelationsSignal.set({
      runtime: [],
      consumed: [30, 31].map((id) => ({
        id,
        slaveId: id + 100,
        relatedResourceName: `Service ${id}`,
        type: 'Webservice',
        relatedResourceRelease: 'current',
        relationName: `relation_${id}`,
        relationType: 'consumed',
      })),
      provided: [],
      unresolved: [unresolved],
    });
    fixture.componentRef.setInput('selectedRelationId', 30);
    component.onItemSelected(component.unresolvedItems()[0]);

    // The router removes rel when an unresolved row is selected.
    fixture.componentRef.setInput('selectedRelationId', null);
    expect(component.selectedUnresolvedRelation()).toEqual(unresolved);
    expect(component.selectedRelation()).toBeNull();

    fixture.componentRef.setInput('selectedRelationId', relationId);

    expect(component.selectedUnresolvedRelation()).toBeNull();
    expect(component.selectedRelation()?.id).toBe(relationId);
    expect(component.selectedItemKey()).toBe(relationId);
  });

  it('uses the type name to resolve an unnamed type relation without matching another named relation', () => {
    resourceSignal.set(currentResource('current'));
    groupedRelationsSignal.set({
      runtime: [],
      consumed: [],
      provided: [],
      unresolved: [{ resRelTypeId: 20, type: 'Webservice', name: 'webservice' }],
    });
    component.onItemSelected(component.unresolvedItems()[0]);
    component.showAddRelationModal();
    component.selectedResourceGroupId.set(2);
    component.addRelation();
    expect(addResourceRelation).toHaveBeenCalledWith(1, 2, false, 'webservice', 'webservice');

    component.onItemSelected({ key: 30, name: 'Existing', type: 'Database' });
    expect(component.selectedUnresolvedRelation()).toBeNull();
    expect(component.getRelationId()).toBe(30);
    component.showAddRelationModal();
    expect(component.resolvingRelation()).toBeNull();
  });

  it('keeps the existing selection when discarding changes is declined', async () => {
    groupedRelationsSignal.set({
      runtime: [],
      consumed: [],
      provided: [],
      unresolved: [{ resRelTypeId: 20, type: 'Webservice', name: 'primary' }],
    });
    vi.spyOn(component, 'hasChanges').mockReturnValue(true);
    component.onItemSelected(component.unresolvedItems()[0]);
    await Promise.resolve();
    expect(TestBed.inject(ConfirmDialogService).confirm).toHaveBeenCalled();
    expect(component.selectedUnresolvedRelation()).toBeNull();
  });
});
