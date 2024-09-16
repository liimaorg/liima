import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FunctionEditComponent } from './function-edit.component';

describe('FunctionsEditComponent', () => {
  let component: FunctionEditComponent;
  let fixture: ComponentFixture<FunctionEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FunctionEditComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FunctionEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
