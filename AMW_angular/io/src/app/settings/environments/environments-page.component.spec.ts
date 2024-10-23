import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EnvironmentsPageComponent } from './environments-page.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('EnvironmentsPageComponent', () => {
  let component: EnvironmentsPageComponent;
  let fixture: ComponentFixture<EnvironmentsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnvironmentsPageComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(EnvironmentsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
