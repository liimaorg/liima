export interface DeploymentDetail {
  deploymentId: number;
  stateMessage: string;
  buildSuccess: boolean;
  executed: boolean;
  deploymentConfirmed: boolean;
  stateToDeploy: number;
}
