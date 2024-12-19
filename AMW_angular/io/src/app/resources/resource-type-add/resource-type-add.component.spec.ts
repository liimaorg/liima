import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResourceTypeAddComponent } from './resource-type-add.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideHttpClient } from '@angular/common/http';

describe('ResourceTypeAddComponent', () => {
  let component: ResourceTypeAddComponent;
  let fixture: ComponentFixture<ResourceTypeAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTypeAddComponent],
      providers: [NgbActiveModal, provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTypeAddComponent);
    component = fixture.componentInstance;

    component.resourceType = {
      id: 0,
      name: '',
      hasChildren: false,
      children: [],
      isApplication: false,
      isDefaultResourceType: false,
    };

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
