import { Deployment } from './deployment';
import { DeploymentParameter } from './deployment-parameter';
import { AppWithVersion } from './app-with-version';
import { DeploymentAction } from './deployment-action';

export interface Deployment {
  id: number;
  trackingId: number;
  state: string,
  deploymentDate: number,
  deploymentJobCreationDate: number,
  deploymentConfirmationDate: number,
  deploymentCancelDate: number,
  appServerName: string,
  appServerId: number,
  appsWithVersion: AppWithVersion[];
  deploymentParameters: DeploymentParameter[];
  environmentName: string;
  releaseName: string;
  runtimeName: string;
  requestUser: string;
  confirmUser: string;
  cancelUser: string;
  deploymentDelayed: boolean;
  actions: DeploymentAction;
}
