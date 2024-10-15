import { TestBed } from '@angular/core/testing';
import { FunctionEditComponent } from './function-edit.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FunctionsService } from './functions.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('FunctionEditComponent', () => {
  let component: FunctionEditComponent;
  let fixture;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FunctionEditComponent],
      providers: [
        NgbActiveModal,
        FunctionsService,
        provideHttpClient(withInterceptorsFromDi())
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FunctionEditComponent);
    component = fixture.componentInstance;

    component.function = {
      id: null,
      name: '',
      content: '',
    };

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
