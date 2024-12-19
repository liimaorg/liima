import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalHeaderComponent } from './modal-header.component';

describe('ModalHeaderComponent', () => {
  let component: ModalHeaderComponent;
  let fixture: ComponentFixture<ModalHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalHeaderComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ModalHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
