import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResourcesListComponent } from './resources-list.component';
import { ComponentRef } from '@angular/core';
import { Resource } from '../../resource/resource';
import { ResourceType } from '../../resource/resource-type';

describe('ResourcesListComponent', () => {
  let component: ResourcesListComponent;
  let componentRef: ComponentRef<ResourcesListComponent>;
  let fixture: ComponentFixture<ResourcesListComponent>;
  const resourceType: ResourceType = {
    id: 1,
    name: 'type',
    hasChildren: false,
    children: [],
    isApplicationResourceType: false,
  };

  const resourceGroupsOfResourceType: Resource[] = [];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourcesListComponent],
      providers: [],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourcesListComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('resourceType', resourceType);
    componentRef.setInput('resourceGroupList', resourceGroupsOfResourceType);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
