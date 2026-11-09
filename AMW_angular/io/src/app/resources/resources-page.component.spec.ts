import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResourcesPageComponent } from './resources-page.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { signal } from '@angular/core';
import { ResourceType } from './models/resource-type';
import { ResourceTypesService } from './services/resource-types.service';
import { ResourceService } from './services/resource.service';

describe('ResourcesPageComponent', () => {
  let component: ResourcesPageComponent;
  let fixture: ComponentFixture<ResourcesPageComponent>;

  const queryParamMap = new BehaviorSubject(convertToParamMap({}));
  const rootResourceTypes = signal<ResourceType[]>([]);
  const setTypeForResourceGroupList = vi.fn();

  beforeEach(async () => {
    queryParamMap.next(convertToParamMap({}));
    rootResourceTypes.set([]);
    setTypeForResourceGroupList.mockClear();
    await TestBed.configureTestingModule({
      imports: [ResourcesPageComponent],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { queryParamMap } },
        {
          provide: Router,
          useValue: {
            navigate: vi.fn((_commands, options) => {
              queryParamMap.next(convertToParamMap(options.queryParams));
              return Promise.resolve(true);
            }),
          },
        },
        {
          provide: ResourceTypesService,
          useValue: { predefinedResourceTypes: signal([]), rootResourceTypes },
        },
        {
          provide: ResourceService,
          useValue: { resourceGroupListForType: signal([]), setTypeForResourceGroupList },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourcesPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it.each([1, 2])(
    'should collapse a parent on the first click after selecting type %i from the URL',
    async (typeId) => {
      vi.useFakeTimers();
      const child: ResourceType = {
        id: 2,
        name: 'Child',
        hasChildren: false,
        hasParent: true,
        children: [],
        isApplication: false,
        isDefaultResourceType: false,
      };
      const parent: ResourceType = {
        ...child,
        id: 1,
        name: 'Parent',
        hasChildren: true,
        hasParent: false,
        children: [child],
      };
      queryParamMap.next(convertToParamMap({ selectedResourceTypeId: typeId }));
      rootResourceTypes.set([parent]);
      fixture.detectChanges();
      await vi.runAllTimersAsync();
      expect(component.selectedResourceType()).toBe(typeId === parent.id ? parent : child);
      expect(component.isExpanded(parent)).toBe(true);
      setTypeForResourceGroupList.mockClear();

      component.toggleChildrenAndOrLoadResourcesList(parent);
      fixture.detectChanges();
      await vi.runAllTimersAsync();

      expect(component.selectedResourceType()).toBe(parent);
      expect(component.isExpanded(parent)).toBe(false);
      expect(setTypeForResourceGroupList).toHaveBeenCalledExactlyOnceWith(parent);

      component.toggleChildrenAndOrLoadResourcesList(parent);
      fixture.detectChanges();
      await vi.runAllTimersAsync();

      expect(component.isExpanded(parent)).toBe(true);

      queryParamMap.next(convertToParamMap({ selectedResourceTypeId: child.id }));
      fixture.detectChanges();
      await vi.runAllTimersAsync();
      expect(component.selectedResourceType()).toBe(child);

      queryParamMap.next(convertToParamMap({ selectedResourceTypeId: parent.id }));
      fixture.detectChanges();
      await vi.runAllTimersAsync();
      expect(component.selectedResourceType()).toBe(parent);
      expect(component.isExpanded(parent)).toBe(true);
    },
  );
});
