import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppsServersListComponent } from './apps-servers-list.component';

describe('AppsListComponent', () => {
  let component: AppsServersListComponent;
  let fixture: ComponentFixture<AppsServersListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppsServersListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AppsServersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
