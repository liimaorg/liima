package ch.puzzle.itc.mobiliar.business.server.boundary;

import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;
import lombok.Getter;

@Getter
public class Server {
    private final String host;
    private final String appServer;
    private final String appServerRelease;
    private final String runtime;
    private final String node;
    private final String nodeRelease;
    private final String environment;
    private final Integer appServerId;
    private final Integer nodeId;
    private final Integer environmentId;
    private final String domain;
    private final Integer domainId;
    private final boolean definedOnNode;

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
