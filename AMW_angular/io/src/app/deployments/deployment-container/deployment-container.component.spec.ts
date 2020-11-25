import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeploymentContainerComponent } from './deployment-container.component';

describe('DeploymentContainerComponent', () => {
  let component: DeploymentContainerComponent;
  let fixture: ComponentFixture<DeploymentContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DeploymentContainerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DeploymentContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
