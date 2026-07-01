import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppsFilterComponent } from './apps-filter.component';

describe('AppsFilterComponent', () => {
  let component: AppsFilterComponent;
  let fixture: ComponentFixture<AppsFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppsFilterComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AppsFilterComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('releases', []);
    fixture.componentRef.setInput('releaseId', 0);
    fixture.componentRef.setInput('filter', '');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
