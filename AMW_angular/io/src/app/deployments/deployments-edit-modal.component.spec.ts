import { TestBed, ComponentFixture } from '@angular/core/testing';
import { DeploymentsEditModalComponent } from './deployments-edit-modal.component';
import { Deployment } from '../deployment/deployment';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { DateTimeModel } from '../shared/date-time-picker/date-time.model';

describe('DeploymentsEditModalComponent', () => {
  let fixture: ComponentFixture<DeploymentsEditModalComponent>;
  let component: DeploymentsEditModalComponent;
  let activeModal: NgbActiveModal;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeploymentsEditModalComponent],
      providers: [NgbActiveModal],
    }).compileComponents();

    fixture = TestBed.createComponent(DeploymentsEditModalComponent);
    component = fixture.componentInstance;
    activeModal = TestBed.inject(NgbActiveModal);
    fixture.detectChanges();
  });

  it('logs unknown edit actions in doEdit()', () => {
    component.selectedEditAction = 'test';
    component.deployments = [{ id: 1, selected: true } as Deployment];
    vi.spyOn(console, 'error');
    vi.spyOn(activeModal, 'close');

    component.doEdit();

    expect(console.error).toHaveBeenCalled();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('applies date for confirmation', () => {
    const newDeploymentDate = DateTimeModel.fromLocalString('30.11.2017 09:19');
    component.deploymentDate = newDeploymentDate;
    component.selectedEditAction = 'Confirm';
    component.deployments = [
      { id: 1, selected: true, deploymentDate: 5555 } as Deployment,
      { id: 2, selected: true, deploymentDate: 6666 } as Deployment,
    ];
    vi.spyOn(activeModal, 'close');

    component.doEdit();

    const deployment1: Deployment = component.deployments[0];
    const deployment2: Deployment = component.deployments[1];
    expect(deployment1.deploymentDate).toEqual(newDeploymentDate.toEpoch());
    expect(deployment2.deploymentDate).toEqual(newDeploymentDate.toEpoch());
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('clears data after doEdit()', () => {
    const newDeploymentDate = DateTimeModel.fromLocalString('30.11.2017 09:19');
    component.deploymentDate = newDeploymentDate;
    component.selectedEditAction = 'Confirm';
    component.deployments = [
      { id: 1, selected: true, deploymentDate: 5555 } as Deployment,
      { id: 2, selected: true, deploymentDate: 6666 } as Deployment,
    ];
    vi.spyOn(activeModal, 'close');

    component.doEdit();

    expect(component.confirmationAttributes).toEqual({} as Deployment);
    expect(component.selectedEditAction).toEqual('');
    expect(component.deploymentDate).toBeUndefined();
    expect(activeModal.close).toHaveBeenCalled();
  });
});
