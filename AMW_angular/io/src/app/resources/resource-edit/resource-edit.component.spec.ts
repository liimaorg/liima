import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceEditComponent } from './resource-edit.component';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';

<<<<<<< HEAD:AMW_angular/io/src/app/resources/resource-edit/resource-edit-page.component.spec.ts
describe('ResourceEditComponent', () => {
=======
describe('ResourceEditPageComponent', () => {
>>>>>>> b2910898... refactor: clean up component naming:AMW_angular/io/src/app/resources/resource-edit/resource-edit.component.spec.ts
  let component: ResourceEditComponent;
  let fixture: ComponentFixture<ResourceEditComponent>;

  const mockRoute: any = { queryParamMap: of() };
  mockRoute.queryParamMap = new Subject<Map<string, number>>();
  mockRoute.queryParamMap.next({
    id: 42,
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceEditComponent, RouterTestingModule.withRoutes([])],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: mockRoute },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
