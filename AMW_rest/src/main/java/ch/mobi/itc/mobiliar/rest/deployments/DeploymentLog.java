package ch.mobi.itc.mobiliar.rest.deployments;

import java.util.Objects;

@Deprecated(forRemoval = true)
public class DeploymentLog {

    private int deploymentId;
    private String filename;

    public DeploymentLog(Integer deploymentId, String filename) {
        this.deploymentId = deploymentId;
        this.filename = filename;
    }

    public int getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(int deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeploymentLog that = (DeploymentLog) o;
        return deploymentId == that.deploymentId && Objects.equals(filename, that.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, filename);
    }
}
