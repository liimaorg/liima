import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PropertyTypesComponent } from './property-types.component';

describe('PropertyTypesComponent', () => {
  let component: PropertyTypesComponent;
  let fixture: ComponentFixture<PropertyTypesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PropertyTypesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PropertyTypesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
