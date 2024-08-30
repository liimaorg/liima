import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PropertyTypeEditComponent } from '../property-types/property-type-edit.component';

describe('PropertyTypeEditComponent', () => {
  let component: PropertyTypeEditComponent;
  const activeModal = new NgbActiveModal();

  beforeEach(async () => {
    component = new PropertyTypeEditComponent(activeModal);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
