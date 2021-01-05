import { DeploymentLog, filenamePredicate, toFileName } from './deployment-log';

describe('deployment log', () => {
  let deploymentLog: DeploymentLog = {
    deploymentId: 1,
    content: 'log content',
    filename: 'logfile.log',
  };

  it('should get filename from deployment log', () => {
    expect(toFileName(deploymentLog)).toBe('logfile.log');
  });

  it('should match deploymentLog', () => {
    const matches = filenamePredicate('logfile.log');
    expect(matches(deploymentLog)).toBeTrue();
  });

  it('should not match deploymentLog', () => {
    const matches = filenamePredicate('other.log');
    expect(matches(deploymentLog)).toBeFalse();
  });
});
