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
  let relationsService: { relations: ReturnType<typeof signal<any>>; relationProperties: ReturnType<typeof signal<any>> };
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
    relationsService = {
      relations: signal({ runtime: [], consumed: [], provided: [], unresolved: [] }),
      relationProperties: signal([]),
    };
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
          },
        },
        {
          provide: ResourceRelationsService,
          useValue: {
            ...relationsService,
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
        {
          provide: UnsavedPropertyChangesService,
          useValue: { setDirty: vi.fn(), discardChangesToken: vi.fn() },
        },
        { provide: ToastService, useValue: { error: vi.fn(), success: vi.fn() } },
        { provide: NgbModal, useValue: { dismissAll: vi.fn() } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceRelationsComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('contextId', 1);
    fixture.detectChanges();
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