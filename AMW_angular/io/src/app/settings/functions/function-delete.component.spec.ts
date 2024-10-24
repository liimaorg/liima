import { FunctionDeleteComponent } from './function-delete.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

describe('FunctionDeleteComponent', () => {
  let component: FunctionDeleteComponent;
  const activeModal = new NgbActiveModal();

  beforeEach(async () => {
    component = new FunctionDeleteComponent(activeModal);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
