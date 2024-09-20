import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AppServerAddComponent } from './app-server-add.component';

describe('AppServerAddComponent', () => {
  let component: AppServerAddComponent;
  const activeModal = new NgbActiveModal();

  beforeEach(async () => {
    component = new AppServerAddComponent(activeModal);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
