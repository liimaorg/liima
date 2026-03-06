import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PropertyEditComponent } from './property-edit.component';

describe('PropertyEditComponent', () => {
  let component: PropertyEditComponent;
  let fixture: ComponentFixture<PropertyEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PropertyEditComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PropertyEditComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
