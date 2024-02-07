import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ReleaseDeleteComponent } from './release-delete.component';

describe('ReleaseDeleteComponent', () => {
  let component: ReleaseDeleteComponent;
  const activeModal = new NgbActiveModal();

  beforeEach(async () => {
    component = new ReleaseDeleteComponent(activeModal);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
