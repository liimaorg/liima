import { TestBed } from '@angular/core/testing';
import { ResourceService } from './resource.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { Resource } from '../models/resource';
import { Release } from '../models/release';

describe('ResourceService', () => {
  let httpTestingController: HttpTestingController;
  let resourceService: ResourceService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [ResourceService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    });

    httpTestingController = TestBed.inject(HttpTestingController);
    resourceService = TestBed.inject(ResourceService);
  });

  it('should have a getAll method', () => {
    expect(resourceService.getAll()).toBeDefined();
  });

  it('should request data from the right endpoint when getAll is called', () => {
    const mockResource: Resource = {
      id: 11,
      name: 'workflow',
      type: 'applicationserver',
      version: '1.0',
      defaultRelease: {
        id: 22,
        release: 'release_2020',
        relations: [],
        properties: [],
        resourceTags: [],
      },
      releases: [],
    };
    resourceService.getAll().subscribe((resourcesRes) => {
      expect(resourcesRes).toEqual([mockResource]);
    });

    const req = httpTestingController.expectOne('/AMW_rest/resources/resources');

    expect(req.request.method).toEqual('GET');

    req.flush([mockResource]);
  });

  it('should request data from the right endpoint when getLatestForRelease is called', () => {
    const mockRelease: Release = {
      id: 55,
      release: 'release2010',
      relations: [
        {
          relatedResourceName: 'relResName',
          relatedResourceRelease: 'relResRelease',
          identifier: 'id',
          type: 'releasetype',
        },
      ],
      properties: [
        {
          name: 'propertyName',
          value: 'propertyValue',
          replacedValue: 'replValue',
          generalComment: 'any comment',
          valueComment: 'comment',
          context: 'context value',
        },
      ],
      resourceTags: [],
    };
    resourceService.getLatestForRelease(1, 2).subscribe((response) => {
      expect(response).toEqual(mockRelease);
    });

    const req = httpTestingController.expectOne('/AMW_rest/resources/resources/resourceGroups/1/releases/2');

    expect(req.request.method).toEqual('GET');

    req.flush(mockRelease);
  });

  it('should request data from the right endpoint when getRuntime is called', () => {
    const mockRelation = {
      relatedResourceName: 'relResourceName',
      relatedResourceRelease: 'relResrourceRelease',
      identifier: 'adam_application_server',
      type: 'applicationserver',
    };
    resourceService.getRuntime('testGroup', 'testRelease').subscribe((response) => {
      expect(response).toEqual([mockRelation]);
    });

    const req = httpTestingController.expectOne(
      '/AMW_rest/resources/resources/testGroup/testRelease/relations?type=RUNTIME',
    );

    expect(req.request.method).toEqual('GET');

    req.flush([mockRelation]);
  });

  it('should request data from the right endpoint when getAppsWithVersions is called', () => {
    const mockAppWithVersions = {
      applicationName: 'adam',
      applicationId: 211558,
      version: '1.9',
    };

    resourceService.getAppsWithVersions(123, 321, [1, 2]).subscribe((response) => {
      expect(response).toEqual([mockAppWithVersions]);
    });

    const req = httpTestingController.expectOne(
      '/AMW_rest/resources/resources/resourceGroups/123/releases/321/appWithVersions/?context=1&context=2',
    );

    expect(req.request.method).toEqual('GET');

    req.flush([mockAppWithVersions]);
  });
});
