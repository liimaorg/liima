import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FunctionsComponent } from './functions.component';

describe('FunctionsComponent', () => {
  let component: FunctionsComponent;
  let fixture: ComponentFixture<FunctionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FunctionsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FunctionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
