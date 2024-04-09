package org.acme.repository;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import org.acme.model.DbConnectionDetails;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class DbConnectionDetailsRepository implements PanacheMongoRepository<DbConnectionDetails> {
    public DbConnectionDetails findByConnectionName(String connectionName) {
            return find("connectionName",connectionName).firstResult();
    }

    public List<DbConnectionDetails> findByWorkSpaceName(String workspacename) {
        return find("workSpaceName",workspacename).stream().collect(Collectors.toUnmodifiableList());
    }

    public long deleteByConnectionName(String connectionName) {
        return delete("connectionName",connectionName);
    }
}
