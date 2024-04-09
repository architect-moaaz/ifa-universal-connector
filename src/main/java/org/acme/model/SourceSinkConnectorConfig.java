package org.acme.model;

import lombok.Data;

@Data
public class SourceSinkConnectorConfig {
    private SourceConfig sourceConfig;
    private SinkConfig sinkConfig;
    private Transforms transforms;
    private String workSpaceName;
    private String name;
    private ConnectorStatus connectorStatus = new ConnectorStatus();
}
