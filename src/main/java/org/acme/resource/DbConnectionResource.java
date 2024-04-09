package org.acme.resource;

import io.smallrye.common.annotation.Blocking;
import org.acme.dto.UserConnectorConfigDto;
import org.acme.resource.utili.ResponseUtil;
import org.acme.service.DbConnectionDetailsService;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/db-connection")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DbConnectionResource {
    private DbConnectionDetailsService dbConnectionDetailsService;

    @Inject
    public DbConnectionResource(DbConnectionDetailsService dbConnectionDetailsService) {
        this.dbConnectionDetailsService = dbConnectionDetailsService;
    }
    @POST
    @Path("/testConnection")
    public ResponseUtil testDbConnection(UserConnectorConfigDto userConnectorConfigDto){
        return dbConnectionDetailsService.testDbConnection(userConnectorConfigDto);
    }

    @POST
    @Path("/addConnection")
    public ResponseUtil addDbConnectionDetails(UserConnectorConfigDto userConnectorConfigDto){
        return dbConnectionDetailsService.addDbDetailsIntoDb(userConnectorConfigDto);
    }

    @GET
    @Path("/by-connection-name/{connectionName}")
    public ResponseUtil getConnectionByName(@PathParam("connectionName") String connectionName){
        return dbConnectionDetailsService.getConnectionByName(connectionName);
    }

    @GET
    @Path("/{workspacename}")
    public List<UserConnectorConfigDto> getConnectionByMiniApp(@PathParam("workspacename") String workspacename){
        return dbConnectionDetailsService.getConnectionByWorkSpaceName(workspacename);
    }

    @DELETE
    @Path("/{connectionName}")
    public ResponseUtil deleteConnection(@PathParam("connectionName") String connectionName){
        return dbConnectionDetailsService.deleteConnection(connectionName);
    }

    @PUT
    public ResponseUtil updateDetails(UserConnectorConfigDto userConnectorConfigDto){
        return dbConnectionDetailsService.updateConnectionDetails(userConnectorConfigDto);
    }

}
