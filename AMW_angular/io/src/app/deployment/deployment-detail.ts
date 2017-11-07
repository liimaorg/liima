export interface DeploymentDetail {
  deploymentId: number;
  stateMessage: string;
  buildSuccess: boolean;
  executed: boolean;
  deploymentConfirmed: boolean;
  stateToDeploy: number;

  sendEmailWhenDeployed: boolean;
  simulateBeforeDeployment: boolean;
  shakedownTestsWhenDeployed: boolean;
  neighbourhoodTest: boolean;
}
