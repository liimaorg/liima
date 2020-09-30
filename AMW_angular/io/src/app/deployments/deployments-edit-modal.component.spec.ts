import { NgZone } from '@angular/core';
import * as moment from 'moment';
import { DeploymentsEditModalComponent } from './deployments-edit-modal.component';
import { Deployment } from '../deployment/deployment';

describe('DeploymentsEditModalComponent (with query params)', () => {
  let component: DeploymentsEditModalComponent;
  beforeEach(() => {
    component = new DeploymentsEditModalComponent();
  });

  it('should log unknown edit actions on doEdit', () => {
    // given
    component.editActions = ['Change date', 'Confirm', 'Reject', 'Cancel'];
    component.selectedEditAction = 'test';
    component.deployments = [{ id: 1, selected: true } as Deployment];
    spyOn(console, 'error');
    spyOn(component, 'hideModal');

    // when
    component.doEdit();

    // then
    expect(console.error).toHaveBeenCalled();
    expect(component.hideModal).toHaveBeenCalled();
  });

  it('should apply date for confirmation', () => {
    // given
    const newDeploymentDate: string = '30.11.2017 09:19';
    const expectedDeploymentDate: number = moment(
      newDeploymentDate,
      'DD.MM.YYYY HH:mm'
    ).valueOf();

    component.editActions = ['Change date', 'Confirm', 'Reject', 'Cancel'];
    component.deploymentDate = newDeploymentDate;
    component.selectedEditAction = 'Confirm';
    component.deployments = [
      { id: 1, selected: true, deploymentDate: 5555 } as Deployment,
      { id: 1, selected: true, deploymentDate: 6666 } as Deployment,
    ];
    spyOn(console, 'error');
    spyOn(component, 'hideModal');

    // when
    component.doEdit();

    // then
    const deployment1: Deployment = component.deployments[0];
    const deployment2: Deployment = component.deployments[1];
    expect(deployment1.deploymentDate).toEqual(expectedDeploymentDate);
    expect(deployment2.deploymentDate).toEqual(expectedDeploymentDate);
    expect(component.hideModal).toHaveBeenCalled();
  });

  it('should clear data after doEdit()', () => {
    // given
    const newDeploymentDate: string = '30.11.2017 09:19';

    component.editActions = ['Change date', 'Confirm', 'Reject', 'Cancel'];
    component.deploymentDate = newDeploymentDate;
    component.selectedEditAction = 'Confirm';
    component.deployments = [
      { id: 1, selected: true, deploymentDate: 5555 } as Deployment,
      { id: 1, selected: true, deploymentDate: 6666 } as Deployment,
    ];
    spyOn(component, 'hideModal');
    // when
    component.doEdit();

    // then
    expect(component.confirmationAttributes).toEqual({} as Deployment);
    expect(component.selectedEditAction).toEqual('');
    expect(component.deploymentDate).toEqual('');
    expect(component.hideModal).toHaveBeenCalled();
  });
});
