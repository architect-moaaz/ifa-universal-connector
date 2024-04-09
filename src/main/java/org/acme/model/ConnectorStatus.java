package org.acme.model;

import lombok.Data;

@Data
public class ConnectorStatus {

    private String sourceConnectorState;
    private String sinkConnectorState;
    private String sourceConnectorTaskStatus;
    private String sinkConnectorTaskStatus;
}
