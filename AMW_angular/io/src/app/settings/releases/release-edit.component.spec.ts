import { ReleaseEditComponent } from './release-edit.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

describe('ReleasesEditComponent', () => {
  let component: ReleaseEditComponent;
  const activeModal = new NgbActiveModal();

  beforeEach(async () => {
    component = new ReleaseEditComponent(activeModal);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
