import { PropertyTypeEditComponent } from './property-type-edit.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

describe('ReleasesEditComponent', () => {
  let component: PropertyTypeEditComponent;
  const activeModal = new NgbActiveModal();

  beforeEach(async () => {
    component = new PropertyTypeEditComponent(activeModal);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
