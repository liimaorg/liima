import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { PropertyEditComponent } from './property-edit.component';

describe('PropertyEditComponent', () => {
  let component: PropertyEditComponent;
  let fixture: ComponentFixture<PropertyEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PropertyEditComponent],
      providers: [NgbActiveModal, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(PropertyEditComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
