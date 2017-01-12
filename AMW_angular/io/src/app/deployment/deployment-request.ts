import { AppWithVersion } from './app-with-version';

export interface DeploymentRequest {
  appServerName: string;
  environmentName: string;
  releaseName: string;
  requestOnly: boolean;
  executeShakedownTest: boolean;
  neighbourhoodTest: boolean;
  sendEmail: boolean;
  appsWithVersion: AppWithVersion[];
  // TODO
  /*
   private Date deploymentDate; // optional
   private Date stateToDeploy; // optional
   private List<AppWithVersionDTO> appsWithVersion;
   private Boolean simulate = false; // optional
   private List<DeploymentParameterDTO> deploymentParameters; // optional
   */

}
