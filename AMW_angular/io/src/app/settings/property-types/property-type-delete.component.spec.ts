import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PropertyTypeDeleteComponent } from './property-type-delete.component';

describe('PropertyTypeDeleteComponent', () => {
  let component: PropertyTypeDeleteComponent;
  const activeModal = new NgbActiveModal();

  beforeEach(async () => {
    component = new PropertyTypeDeleteComponent(activeModal);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
