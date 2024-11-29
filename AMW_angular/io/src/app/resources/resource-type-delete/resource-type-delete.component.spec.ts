import { ResourceTypeDeleteComponent } from './resource-type-delete.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ComponentFixture, TestBed } from '@angular/core/testing';

describe('ResourceTypeDeleteComponent', () => {
  let component: ResourceTypeDeleteComponent;
  let fixture: ComponentFixture<ResourceTypeDeleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTypeDeleteComponent],
      providers: [NgbActiveModal],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTypeDeleteComponent);
    component = fixture.componentInstance;

    component.resourceType = {
      id: 1258,
      name: 'Keystore',
      hasChildren: false,
      children: [],
      resourceTypeIsApplication: false,
    };

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
