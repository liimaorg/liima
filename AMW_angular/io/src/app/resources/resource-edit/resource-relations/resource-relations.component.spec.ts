import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { signal } from '@angular/core';
import { ResourceRelationsComponent } from './resource-relations.component';
import { ResourceService } from '../../services/resource.service';
import { ResourceTypesService } from '../../services/resource-types.service';
import { ResourceRelationsService } from '../../services/resource-relations.service';
import { AuthService } from 'src/app/auth/auth.service';
import { ToastService } from 'src/app/shared/elements/toast/toast.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';

describe('ResourceRelationsComponent', () => {
  let component: ResourceRelationsComponent;
  let fixture: ComponentFixture<ResourceRelationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceRelationsComponent],
      providers: [
        ResourceService,
        ResourceTypesService,
        ResourceRelationsService,
        AuthService,
        ToastService,
        NgbModal,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        {
          provide: Router,
          useValue: { navigate: vi.fn() },
        },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({}) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceRelationsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load resource types for default resource type (APPLICATION)', () => {
    component['resource'] = signal({
      id: 1,
      name: 'Test',
      type: 'APPLICATION',
      version: '1.0',
      defaultRelease: {} as any,
      releases: [],
    });
    const spy = vi.spyOn(component, 'loadAllResourceTypes' as any).mockImplementation(() => {});

    component.showAddRelationModal();

    expect(spy).toHaveBeenCalled();
  });

  it('should load resource types for default resource type (APPLICATIONSERVER)', () => {
    component['resource'] = signal({
      id: 1,
      name: 'Test',
      type: 'APPLICATIONSERVER',
      version: '1.0',
      defaultRelease: {} as any,
      releases: [],
    });
    const spy = vi.spyOn(component, 'loadAllResourceTypes' as any).mockImplementation(() => {});

    component.showAddRelationModal();

    expect(spy).toHaveBeenCalled();
  });

  it('isNode should return true for NODE resource type', () => {
    component['resource'] = signal({
      id: 1,
      name: 'Test',
      type: 'NODE',
      version: '1.0',
      defaultRelease: {} as any,
      releases: [],
    });

    expect(component['isNode']()).toBe(true);
  });

  it('isNode should return true for quoted NODE resource type', () => {
    component['resource'] = signal({
      id: 1,
      name: 'Test',
      type: '"NODE"',
      version: '1.0',
      defaultRelease: {} as any,
      releases: [],
    });

    expect(component['isNode']()).toBe(true);
  });

  it('isNode should return false for non-NODE resource type', () => {
    component['resource'] = signal({
      id: 1,
      name: 'Test',
      type: 'APPLICATION',
      version: '1.0',
      defaultRelease: {} as any,
      releases: [],
    });

    expect(component['isNode']()).toBe(false);
  });

  it('should load resource types for default resource type (RUNTIME)', () => {
    component['resource'] = signal({
      id: 1,
      name: 'Test',
      type: 'RUNTIME',
      version: '1.0',
      defaultRelease: {} as any,
      releases: [],
    });
    const spy = vi.spyOn(component, 'loadAllResourceTypes' as any).mockImplementation(() => {});

    component.showAddRelationModal();

    expect(spy).toHaveBeenCalled();
  });

  it('should load related resource types for non-default resource type', () => {
    component['resource'] = signal({
      id: 1,
      name: 'Test',
      type: 'Webservice',
      version: '1.0',
      defaultRelease: {} as any,
      releases: [],
    });
    component['groupedRelations'] = signal({
      runtime: [],
      consumed: [],
      provided: [],
      unresolved: [{ type: 'FeatureTeam', name: 'Team A' }],
    } as any);
    const spy = vi.spyOn(component, 'loadRelatedResourceTypes' as any).mockImplementation(() => {});

    component.showAddRelationModal();

    expect(spy).toHaveBeenCalled();
  });

  it('should handle quoted type names (e.g., "APPLICATION")', () => {
    component['resource'] = signal({
      id: 1,
      name: 'Test',
      type: '"APPLICATION"',
      version: '1.0',
      defaultRelease: {} as any,
      releases: [],
    });
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
});
