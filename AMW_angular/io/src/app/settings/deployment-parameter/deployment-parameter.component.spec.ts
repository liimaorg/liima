import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { DeploymentParameterComponent } from './deployment-parameter.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('DeploymentParameterComponent', () => {
  let component: DeploymentParameterComponent;
  let fixture: ComponentFixture<DeploymentParameterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
    imports: [DeploymentParameterComponent],
    providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
}).compileComponents();

    fixture = TestBed.createComponent(DeploymentParameterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
