import { Deployment } from './deployment';

export interface Deployment {
  appServerId: number
  releaseId: number;
  trackingId: string,
  state: string,
  deploymentDate: Date,
  environmentIds: number[]
  doSendEmail: boolean;
  doExecuteShakedownTest: boolean;
  doNeighbourhoodTest: boolean;
}
