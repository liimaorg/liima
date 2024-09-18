import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppsListComponent } from './apps-list.component';

describe('AppsListComponent', () => {
  let component: AppsListComponent;
  let fixture: ComponentFixture<AppsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppsListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AppsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
