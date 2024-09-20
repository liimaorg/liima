import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppAddComponent } from './app-add.component';

describe('AppAddComponent', () => {
  let component: AppAddComponent;
  let fixture: ComponentFixture<AppAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppAddComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AppAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
