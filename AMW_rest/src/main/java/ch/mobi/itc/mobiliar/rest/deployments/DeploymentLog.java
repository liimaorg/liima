package ch.mobi.itc.mobiliar.rest.deployments;

public class DeploymentLog {

    private int deploymentId;
    private String filename;
    private String content;

    public DeploymentLog(Integer id, String filename, String deploymentLog) {
        this.deploymentId = id;
        this.filename = filename;
        this.content = deploymentLog;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeploymentLog that = (DeploymentLog) o;

        if (deploymentId != that.deploymentId) return false;
        if (filename != null ? !filename.equals(that.filename) : that.filename != null) return false;
        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        int result = deploymentId;
        result = 31 * result + (filename != null ? filename.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
