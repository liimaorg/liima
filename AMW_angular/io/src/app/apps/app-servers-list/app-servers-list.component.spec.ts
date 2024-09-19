import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppServersListComponent } from './app-servers-list.component';

describe('AppsListComponent', () => {
  let component: AppServersListComponent;
  let fixture: ComponentFixture<AppServersListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppServersListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AppServersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
