package ch.puzzle.itc.mobiliar.business.server.boundary;

import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;
import lombok.Getter;

@Getter
public class Server {
    private String host;
    private String appServer;
    private String appServerRelease;
    private String runtime;
    private String node;
    private String nodeRelease;
    private String environment;
    private Integer appServerId;
    private Integer nodeId;
    private Integer environmentId;
    private String domain;
    private Integer domainId;
    private boolean definedOnNode;

    public Server(ServerTuple serverTuple) {
        this.host = serverTuple.getHost();
        this.appServer = serverTuple.getAppServer();
        this.appServerRelease = serverTuple.getAppServerRelease();
        this.runtime = serverTuple.getRuntime();
        this.node = serverTuple.getNode();
        this.nodeRelease = serverTuple.getNodeRelease();
        this.environment = serverTuple.getEnvironment();
        this.appServerId = serverTuple.getAppServerId();
        this.nodeId = serverTuple.getNodeId();
        this.environmentId = serverTuple.getEnvironmentId();
        this.domain = serverTuple.getDomain();
        this.domainId = serverTuple.getDomainId();
        this.definedOnNode = serverTuple.isDefinedOnNode();
    }
}
