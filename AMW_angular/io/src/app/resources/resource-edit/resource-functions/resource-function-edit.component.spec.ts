import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResourceFunctionEditComponent } from './resource-function-edit.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ResourceFunctionsService } from '../../services/resource-functions.service';

describe('FunctionEditComponent', () => {
  let component: ResourceFunctionEditComponent;
  let fixture: ComponentFixture<ResourceFunctionEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceFunctionEditComponent],
      providers: [NgbActiveModal, ResourceFunctionsService, provideHttpClient(withInterceptorsFromDi())],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceFunctionEditComponent);
    component = fixture.componentInstance;

    component.function = {
      definedOnResource: false,
      definedOnResourceType: false,
      isOverwritingFunction: false,
      id: null,
      name: '',
      miks: new Set<string>(),
      content: '',
    };

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
