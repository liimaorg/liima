import { TestBed } from '@angular/core/testing';
import { ResourceActivationService, ResourceActivation } from './resource-activation.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('ResourceActivationService', () => {
  let httpTestingController: HttpTestingController;
  let service: ResourceActivationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ResourceActivationService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    });

    httpTestingController = TestBed.inject(HttpTestingController);
    service = TestBed.inject(ResourceActivationService);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getActivations', () => {
    it('should fetch activations from the correct endpoint', () => {
      const mockActivations: ResourceActivation[] = [
        { resourceGroupId: 1, resourceGroupName: 'App1', active: true },
        { resourceGroupId: 2, resourceGroupName: 'App2', active: false },
      ];

      service.getActivations(100, 200, 1).subscribe((activations) => {
        expect(activations).toEqual(mockActivations);
      });

      const req = httpTestingController.expectOne('/AMW_rest/resources/100/relations/200/activations?contextId=1');
      expect(req.request.method).toEqual('GET');
      req.flush(mockActivations);
    });

    it('should use default contextId when not provided', () => {
      const mockActivations: ResourceActivation[] = [];

      service.getActivations(100, 200, 1).subscribe((activations) => {
        expect(activations).toEqual(mockActivations);
      });

      const req = httpTestingController.expectOne('/AMW_rest/resources/100/relations/200/activations?contextId=1');
      expect(req.request.method).toEqual('GET');
      req.flush(mockActivations);
    });
  });

  describe('updateActivations', () => {
    it('should send PUT request to the correct endpoint', () => {
      const request = { activeResourceGroupIds: [1, 2, 3] };

      service.updateActivations(100, 200, 5, request).subscribe(() => {
        // Success
      });

      const req = httpTestingController.expectOne('/AMW_rest/resources/100/relations/200/activations?contextId=5');
      expect(req.request.method).toEqual('PUT');
      expect(req.request.body).toEqual(request);
      req.flush(null);
    });
  });

  describe('activations signal', () => {
    it('should update activations signal when setRelationParams is called', () => {
      const mockActivations: ResourceActivation[] = [{ resourceGroupId: 1, resourceGroupName: 'App1', active: true }];

      service.setRelationParams(100, 200, 1);

      const req = httpTestingController.expectOne('/AMW_rest/resources/100/relations/200/activations?contextId=1');
      req.flush(mockActivations);

      expect(service.activations()).toEqual(mockActivations);
    });
  });

  describe('isLoading signal', () => {
    it('should be true during request and false after', () => {
      const mockActivations: ResourceActivation[] = [];

      expect(service.isLoading()).toBe(false);

      service.setRelationParams(100, 200, 1);

      expect(service.isLoading()).toBe(true);

      const req = httpTestingController.expectOne('/AMW_rest/resources/100/relations/200/activations?contextId=1');
      req.flush(mockActivations);

      expect(service.isLoading()).toBe(false);
    });
  });
});
