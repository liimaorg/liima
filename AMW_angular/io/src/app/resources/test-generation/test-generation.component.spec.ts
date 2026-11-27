import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { signal } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { Resource } from '../models/resource';
import { ResourceService } from '../services/resource.service';
import { TestGenerationService } from '../services/test-generation.service';
import { EnvironmentService } from '../../deployment/environment.service';
import { ReleasesService } from '../../settings/releases/releases.service';
import { Release as GlobalRelease } from '../../settings/releases/release';
import { Release } from '../models/release';
import { TestGenerationComponent } from './test-generation.component';

describe('TestGenerationComponent', () => {
  let fixture: ComponentFixture<TestGenerationComponent>;
  let resourceSignal: ReturnType<typeof signal<Resource | null>>;
  let releasesSignal: ReturnType<typeof signal<GlobalRelease[]>>;
  let generateTest: ReturnType<typeof vi.fn>;
  let getReleasesForResource: ReturnType<typeof vi.fn>;
  const queryParamMap = new BehaviorSubject(convertToParamMap({ id: '42', ctx: '1' }));

  beforeEach(async () => {
    queryParamMap.next(convertToParamMap({ id: '42', ctx: '1' }));
    resourceSignal = signal<Resource | null>(null);
    releasesSignal = signal<GlobalRelease[]>([
      {
        id: 1,
        name: 'RL-25.10',
        mainRelease: false,
        description: '',
        installationInProductionAt: 1000,
        v: null,
        default: false,
      },
      {
        id: 2,
        name: 'RL-26.01',
        mainRelease: false,
        description: '',
        installationInProductionAt: 2000,
        v: null,
        default: false,
      },
      {
        id: 3,
        name: 'RL-27.01',
        mainRelease: false,
        description: '',
        installationInProductionAt: 3000,
        v: null,
        default: false,
      },
    ]);
    generateTest = vi.fn(() =>
      of({ releaseName: '', applicationServerName: '', nodeGenerationResults: [], error: '' }),
    );
    getReleasesForResource = vi.fn(() =>
      of<Release[]>([{ id: 42, name: 'RL-25.10', installationInProductionAt: 1000 }]),
    );
    await TestBed.configureTestingModule({
      imports: [TestGenerationComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { queryParamMap } },
        {
          provide: ResourceService,
          useValue: {
            resource: resourceSignal,
            setIdForResource: vi.fn(),
            getReleasesForResource,
          },
        },
        { provide: TestGenerationService, useValue: { generateTest } },
        {
          provide: EnvironmentService,
          useValue: { environmentTree: signal([]), findEnvironmentById: vi.fn(() => ({ name: 'DEV' })) },
        },
        { provide: ReleasesService, useValue: { allReleases: releasesSignal } },
      ],
    });
    TestBed.overrideComponent(TestGenerationComponent, { set: { template: '' } });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(TestGenerationComponent);
    fixture.detectChanges();
  });

  it('should include the resource name in the page title', () => {
    expect(TestBed.inject(Title).getTitle()).toBe('Test Generation - Liima');

    resourceSignal.set({ id: 42, name: 'Orders' } as Resource);
    fixture.detectChanges();

    expect(TestBed.inject(Title).getTitle()).toBe('Test Generation for Orders - Liima');

    queryParamMap.next(convertToParamMap({ id: '43', ctx: '1' }));
    fixture.detectChanges();
    expect(TestBed.inject(Title).getTitle()).toBe('Test Generation for Orders - Liima');

    resourceSignal.set({ id: 43, name: 'Invoices' } as Resource);
    fixture.detectChanges();
    expect(TestBed.inject(Title).getTitle()).toBe('Test Generation for Invoices - Liima');
  });

  it('generates and compares using selected global releases', () => {
    const component = fixture.componentInstance;
    resourceSignal.set({ id: 42, name: 'Orders', release: 'RL-25.10' } as Resource);
    fixture.detectChanges();
    expect(component.releaseName()).toBe('RL-25.10');
    expect(component.releases().map((release) => release.name)).toEqual(['RL-25.10', 'RL-26.01', 'RL-27.01']);

    component.setRelease('RL-26.01');
    component.setCompareRelease('RL-27.01');
    generateTest.mockClear();

    component.generate();

    expect(generateTest).toHaveBeenNthCalledWith(1, 'Orders', 'RL-26.01', 'DEV');
    expect(generateTest).toHaveBeenNthCalledWith(2, 'Orders', 'RL-27.01', 'DEV');
  });

  it('excludes releases older than the oldest app server release, regardless of list order', () => {
    getReleasesForResource.mockReturnValue(
      of<Release[]>([
        { id: 43, name: 'RL-27.01', installationInProductionAt: 3000 },
        { id: 42, name: 'RL-26.01', installationInProductionAt: 2000 },
      ]),
    );
    releasesSignal.update((releases) => [
      ...releases,
      { ...releases[1], id: 4, name: 'Same date', installationInProductionAt: 2000 },
    ]);
    resourceSignal.set({ id: 42, name: 'Orders', release: 'RL-26.01' } as Resource);
    fixture.detectChanges();

    expect(fixture.componentInstance.releases().map((release) => release.name)).toEqual([
      'RL-26.01',
      'RL-27.01',
      'Same date',
    ]);
  });

  it('uses the parent app server releases when opened from an application', () => {
    getReleasesForResource.mockReturnValue(
      of<Release[]>([{ id: 84, name: 'RL-26.01', installationInProductionAt: 2000 }]),
    );
    resourceSignal.set({ id: 42, name: 'Orders', release: 'RL-27.01', applicationServerId: 84 } as Resource);
    fixture.detectChanges();

    expect(getReleasesForResource).toHaveBeenCalledWith(84);
    expect(fixture.componentInstance.releases().map((release) => release.name)).toEqual(['RL-26.01', 'RL-27.01']);
  });

  it('reports app server release loading failures instead of showing unrestricted releases', () => {
    getReleasesForResource.mockReturnValue(throwError(() => new Error('Failed')));
    resourceSignal.set({ id: 42, name: 'Orders', release: 'RL-25.10' } as Resource);
    fixture.detectChanges();

    expect(fixture.componentInstance.releases()).toEqual([]);
    expect(fixture.componentInstance.releaseErrorMessage()).toBe('Failed to load application server releases.');
  });
});
