package org.acme.model;

import lombok.Data;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Data
public class SourceConfig {
    private ObjectId id;
    private String userId;
    private String port;
    private String connector;
    private String host;
    private String connectorType;
    private String dbUser;
    private String dbPassword;
    private String dbName;
    private String workSpaceId;
    private String workSpaceName;
    private String miniAppName;
    private String appId;
    private String topics;
    private String name;
    private String configuration;
    private List<String> tables;
    private Map<String,List<String>> columns;
    private String connectionName;
    private String primaryKeys;
    private String connectorFamily;
    private String topicsPrefix;
    private String status;
    private String inputPathForSftp;
    private String finishedPathForSftp;
    private String errorPathForSftp;
    private String fileName;
    private String connectorStatus;
    private String tasksStatus;
    private String dbServer;
}
