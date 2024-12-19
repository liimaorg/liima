package ch.mobi.itc.mobiliar.rest.dtos;


import ch.puzzle.itc.mobiliar.business.server.entity.ServerTuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "appAppServer")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerDTO {

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
    private boolean definedOnNode;

    public ServerDTO(ServerTuple serverTuple) {
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
        this.definedOnNode = serverTuple.isDefinedOnNode();
    }
}
