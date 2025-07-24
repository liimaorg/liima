import { ResourceFunctionDeleteComponent } from './resource-function-delete.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ComponentFixture, TestBed } from '@angular/core/testing';

describe('ResourceFunctionDeleteComponent', () => {
  let component: ResourceFunctionDeleteComponent;
  let fixture: ComponentFixture<ResourceFunctionDeleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceFunctionDeleteComponent],
      providers: [NgbActiveModal],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceFunctionDeleteComponent);
    component = fixture.componentInstance;

    component.functionId = 46 + 2;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
