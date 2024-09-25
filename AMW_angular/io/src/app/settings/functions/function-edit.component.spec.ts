import { FunctionEditComponent } from './function-edit.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

describe('FunctionEditComponent', () => {
  let component: FunctionEditComponent;
  const activeModal = new NgbActiveModal();

  beforeEach(async () => {
    component = new FunctionEditComponent(activeModal);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
