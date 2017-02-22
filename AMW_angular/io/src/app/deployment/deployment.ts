import { Deployment } from './deployment';
import { DeploymentParameter } from './deployment-parameter';
import { AppWithVersion } from './app-with-version';

export interface Deployment {
  id: number;
  trackingId: number;
  state: string,
  deploymentDate: Date,
  appServerName: string,
  appsWithVersion: AppWithVersion[];
  deploymentParameters: DeploymentParameter[];
  environmentName: string;
  releaseName: string;
  runtimeName: string;
  requestUser: string;
  confirmUser: string;
  cancleUser: string;
}
