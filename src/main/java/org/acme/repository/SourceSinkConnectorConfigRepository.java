package org.acme.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import org.acme.model.SourceSinkConnectorConfig;
import org.acme.model.UserConnectorConfig;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SourceSinkConnectorConfigRepository implements PanacheMongoRepository<SourceSinkConnectorConfig> {
//    public List<UserConnectorConfig> findByMiniAppName(String miniAppName) {
//        return find("miniAppName",miniAppName).list();
//    }
    public List<SourceSinkConnectorConfig> findByWorkSpaceName(String workspacename) {
        return find("workSpaceName",workspacename).list();
    }

    public SourceSinkConnectorConfig findByName(String name) {
        return find("name",name).firstResult();
    }

    public long deleteByName(String connectorName) {
        return delete("name",connectorName);
    }
}
