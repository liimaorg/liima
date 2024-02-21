import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DeploymentParameterComponent } from './deployment-parameter.component';

describe('DeploymentParameterComponent', () => {
  let component: DeploymentParameterComponent;
  let fixture: ComponentFixture<DeploymentParameterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeploymentParameterComponent, HttpClientTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(DeploymentParameterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
