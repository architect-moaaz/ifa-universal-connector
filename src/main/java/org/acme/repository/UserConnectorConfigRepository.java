package org.acme.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import org.acme.model.UserConnectorConfig;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class UserConnectorConfigRepository implements PanacheMongoRepository<UserConnectorConfig> {

    public List<UserConnectorConfig> findByMiniAppName(String miniAppName) {
        return find("miniAppName",miniAppName).list();
    }
    public List<UserConnectorConfig> findByWorkSpaceName(String workspacename) {
        return find("workSpaceName",workspacename).list();
    }

    public UserConnectorConfig findByName(String name) {
        return find("name",name).firstResult();
    }

    public long deleteByName(String connectorName) {
        return delete("name",connectorName);
    }
}
