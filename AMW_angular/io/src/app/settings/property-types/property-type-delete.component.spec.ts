import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PropertyTypeDeleteComponent } from './property-type-delete.component';

describe('PropertyTypeDeleteComponent', () => {
  let component: PropertyTypeDeleteComponent;
  let fixture: ComponentFixture<PropertyTypeDeleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PropertyTypeDeleteComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PropertyTypeDeleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
