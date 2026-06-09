import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ComponentRef } from '@angular/core';
import { of } from 'rxjs';

import { RelationActiveApplicationsComponent } from './relation-active-applications.component';
import { ResourceActivationService, ResourceActivation } from '../../../services/resource-activation.service';

describe('RelationActiveApplicationsComponent', () => {
  let component: RelationActiveApplicationsComponent;
  let fixture: ComponentFixture<RelationActiveApplicationsComponent>;
  let componentRef: ComponentRef<RelationActiveApplicationsComponent>;
  let mockActivationService: jasmine.SpyObj<ResourceActivationService>;

  const mockActivations: ResourceActivation[] = [
    { resourceGroupId: 1, resourceGroupName: 'Application A', active: true },
    { resourceGroupId: 2, resourceGroupName: 'Application B', active: false },
    { resourceGroupId: 3, resourceGroupName: 'Application C', active: true },
  ];

  beforeEach(async () => {
    mockActivationService = jasmine.createSpyObj('ResourceActivationService', [
      'setRelationParams',
    ], {
      activations: mockActivations,
      isLoading: false,
    });

    await TestBed.configureTestingModule({
      imports: [RelationActiveApplicationsComponent],
      providers: [
        { provide: ResourceActivationService, useValue: mockActivationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RelationActiveApplicationsComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;

    // Set required inputs
    componentRef.setInput('resourceId', 100);
    componentRef.setInput('relationId', 200);
    componentRef.setInput('contextId', 1);
    componentRef.setInput('isApplicationServerToNode', true);
    componentRef.setInput('canEdit', true);
    componentRef.setInput('activeApplicationIds', [1, 3]);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set relation params on service when inputs change', () => {
    expect(mockActivationService.setRelationParams).toHaveBeenCalledWith(100, 200, 1);
  });

  it('should compute activations from service', () => {
    expect(component.activations()).toEqual(mockActivations);
  });

  it('should compute hasApplications based on activations', () => {
    expect(component.hasApplications()).toBe(true);

    // Test empty activations
    mockActivationService.activations = [];
    fixture.detectChanges();
    expect(component.hasApplications()).toBe(false);
  });

  it('should determine if application is active', () => {
    expect(component.isApplicationActive(1)).toBe(true);
    expect(component.isApplicationActive(2)).toBe(false);
    expect(component.isApplicationActive(3)).toBe(true);
  });

  describe('onCheckboxChange', () => {
    it('should emit activationChange with added app ID when checked', () => {
      spyOn(component.activationChange, 'emit');

      component.onCheckboxChange(2, true);

      expect(component.activationChange.emit).toHaveBeenCalledWith([1, 3, 2]);
    });

    it('should emit activationChange with removed app ID when unchecked', () => {
      spyOn(component.activationChange, 'emit');

      component.onCheckboxChange(1, false);

      expect(component.activationChange.emit).toHaveBeenCalledWith([3]);
    });

    it('should handle multiple changes correctly', () => {
      spyOn(component.activationChange, 'emit');

      // First uncheck app 1
      component.onCheckboxChange(1, false);
      expect(component.activationChange.emit).toHaveBeenCalledWith([3]);

      // Then check app 2
      componentRef.setInput('activeApplicationIds', [3]);
      component.onCheckboxChange(2, true);
      expect(component.activationChange.emit).toHaveBeenCalledWith([3, 2]);
    });
  });

  it('should inherit isLoading from service', () => {
    expect(component.isLoading()).toBe(false);
  });

  describe('conditional rendering', () => {
    it('should not render when isApplicationServerToNode is false', () => {
      componentRef.setInput('isApplicationServerToNode', false);
      fixture.detectChanges();

      const section = fixture.nativeElement.querySelector('.active-applications-section');
      expect(section).toBeFalsy();
    });

    it('should not render when hasApplications is false', () => {
      mockActivationService.activations = [];
      fixture.detectChanges();

      const section = fixture.nativeElement.querySelector('.active-applications-section');
      expect(section).toBeFalsy();
    });

    it('should render when conditions are met', () => {
      const section = fixture.nativeElement.querySelector('.active-applications-section');
      expect(section).toBeTruthy();

      const heading = section.querySelector('h4');
      expect(heading.textContent).toContain('Active Applications');
    });

    it('should render checkboxes for each application', () => {
      const checkboxes = fixture.nativeElement.querySelectorAll('input[type="checkbox"]');
      expect(checkboxes.length).toBe(3);
    });

    it('should disable checkboxes when canEdit is false', () => {
      componentRef.setInput('canEdit', false);
      fixture.detectChanges();

      const checkbox = fixture.nativeElement.querySelector('input[type="checkbox"]');
      expect(checkbox.disabled).toBe(true);
    });

    it('should enable checkboxes when canEdit is true', () => {
      const checkbox = fixture.nativeElement.querySelector('input[type="checkbox"]');
      expect(checkbox.disabled).toBe(false);
    });
  });
});
