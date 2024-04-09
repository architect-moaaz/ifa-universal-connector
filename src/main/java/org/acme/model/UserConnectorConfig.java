package org.acme.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MongoEntity(collection = "userconnectorconfig")
public class UserConnectorConfig{

//    @BsonProperty("id")
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
    private String appId;
    private String topics;
    private String name;
    private String configuration;
    private List<String> tables;
    private Map<String,List<String>> columns;
    private String workSpaceName;
    private String miniAppName;
    private String primaryKeys;
    private String connectorFamily;
    private String topicsPrefix;
}
