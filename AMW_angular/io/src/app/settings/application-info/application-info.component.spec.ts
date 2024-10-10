import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationInfoComponent } from './application-info.component';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('ApplicationInfoComponent', () => {
  let component: ApplicationInfoComponent;
  let fixture: ComponentFixture<ApplicationInfoComponent>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationInfoComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
