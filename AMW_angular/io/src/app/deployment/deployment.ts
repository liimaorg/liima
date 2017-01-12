import { Deployment } from './deployment';
import { AppWithVersion } from './app-with-version';

export interface Deployment {
  id: number;
  trackingId: number;
  state: string,
  deploymentDate: Date,
  appServerName: string,
  appsWithVersion: AppWithVersion[];
  environmentName: string;
  releaseName: string;
  runtimeName: string;
  requestUser: string;
  confirmUser: string;
  cancleUser: string;
}

/*
 // TODO
 deploymentParams = {LinkedList@20568}  size = 0
 nodeJobs = {HashSet@20575}  size = 0
 */
