export interface DeploymentLog {
  deploymentId: number;
  filename: string;
  content: string;
}

export const toFileName = (deploymentLog: DeploymentLog): string =>
  deploymentLog.filename;

export const filenamePredicate = (filename: string) => (
  deploymentLog: DeploymentLog
): boolean => deploymentLog.filename === filename;
