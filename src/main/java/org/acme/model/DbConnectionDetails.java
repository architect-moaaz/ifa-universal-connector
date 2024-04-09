package org.acme.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MongoEntity(collection = "dbconfig")
public class DbConnectionDetails {
    private ObjectId id;
    private String dbUser;
    private String dbPassword;
    private String dbName;
    private String host;
    private String port;
    private String workSpaceName;
    private String miniAppName;
    private String Db;
    private String connectionName;
}
