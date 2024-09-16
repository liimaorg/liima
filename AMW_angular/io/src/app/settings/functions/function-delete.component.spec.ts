import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FunctionDeleteComponent } from './function-delete.component';

describe('FunctionsDeleteComponent', () => {
  let component: FunctionDeleteComponent;
  let fixture: ComponentFixture<FunctionDeleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FunctionDeleteComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FunctionDeleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
