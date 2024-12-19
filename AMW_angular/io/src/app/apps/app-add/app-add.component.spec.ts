import { AppAddComponent } from './app-add.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

describe('AppAddComponent', () => {
  let component: AppAddComponent;
  const activeModal = new NgbActiveModal();

  beforeEach(async () => {
    component = new AppAddComponent(activeModal);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
