package org.acme.resource;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import org.acme.dto.UserConnectorConfigDto;
import org.acme.model.DataModelDTO;
import org.acme.model.SourceSinkConnectorConfig;
import org.acme.resource.utili.ConnectorEnum;
import org.acme.resource.utili.EventResponseModel;
import org.acme.resource.utili.ResponseUtil;
import org.acme.resource.utili.SinkConnectionDetails;
import org.acme.service.UserConnectorService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@Path("/connector")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Blocking
public class UserConnectorResource {
    @Inject
    @ConfigProperty(name = "quarkus.mongodb.database")
    public String DbName;

    @Inject
    @ConfigProperty(name = "quarkus.mongodb.credentials.username")
    public String DbUser;

    @Inject
    @ConfigProperty(name = "quarkus.mongodb.credentials.password")
    public String DbPassword;

    @Inject
    @ConfigProperty(name = "mongo.host")
    public String Host;

    @Inject
    @ConfigProperty(name = "mongo.port")
    public String Port;

    @Inject
    @ConfigProperty(name = "kafka.server.host")
    public String kafkaHost;

    @Inject
    @ConfigProperty(name = "kafka.server.port")
    public String kafkaPort;
    private UserConnectorService userConnectorService;

    private SinkConnectionDetails sinkConnectionDetails = new SinkConnectionDetails();

    @Inject
    public UserConnectorResource(UserConnectorService userConnectorService) {
        this.userConnectorService = userConnectorService;
    }

    @POST
    public Uni<EventResponseModel> addConnector(UserConnectorConfigDto userConnectorConfigDto) throws SQLException, ClassNotFoundException {
        if (userConnectorConfigDto.getConnectorType().equals(ConnectorEnum.SOURCE.toString()))
            return userConnectorService.addSourceConnector(userConnectorConfigDto);
        return userConnectorService.addSinkConnector(userConnectorConfigDto);
    }

    @POST
    @Path("/oneclick")
    public Uni<EventResponseModel> addConnectorOnOneClick(SourceSinkConnectorConfig sourceSinkConnectorConfig) throws SQLException, ClassNotFoundException {
        EventResponseModel eventResponseModel = new EventResponseModel();
        try{
            SinkConnectionDetails sinkConnectionDetails = new SinkConnectionDetails();
            sinkConnectionDetails = setProperties(sinkConnectionDetails);
            System.out.println(sinkConnectionDetails);
            return userConnectorService.addConnectorOneClick(sourceSinkConnectorConfig,sinkConnectionDetails);
        }catch (Exception e){
	e.printStackTrace();
            eventResponseModel.setMessage("Please check your payload few values are missing");
            eventResponseModel.setStatus(500);
            return Uni.createFrom().item(eventResponseModel);
        }
    }

    private  SinkConnectionDetails setProperties(SinkConnectionDetails sinkConnectionDetails) {
        sinkConnectionDetails.setConnector("MONGODB");
        sinkConnectionDetails.setDbName(DbName);
        sinkConnectionDetails.setDbUser(DbUser);
        sinkConnectionDetails.setDbPassword(DbPassword);
        sinkConnectionDetails.setHost(Host);
        sinkConnectionDetails.setPort(Port);
        sinkConnectionDetails.setMiniAppName("Demo");
        sinkConnectionDetails.setWorkSpaceName("Intelliflow");
        sinkConnectionDetails.setKafkaHost(kafkaHost);
        sinkConnectionDetails.setKafkaPort(kafkaPort);
        return sinkConnectionDetails;
    }

    @GET
    @Path("/{name}")
    public ResponseUtil getConnectorByName(@PathParam("name") String name) {
        return userConnectorService.getConnectorByName(name);
    }

    @GET
    @Path("/oneclick/{name}")
    public ResponseUtil getConnectorByNameOneClick(@PathParam("name") String name) {
        return userConnectorService.getConnectorByNameOneClick(name);
    }

    @DELETE
    @Path("/{connector}")
    public ResponseUtil deleteConfig(@PathParam("connector") String connectorName) {
        return userConnectorService.deleteConfig(connectorName);
    }

    @DELETE
    @Path("/oneclick/{connector}")
    public ResponseUtil deleteConfigOneClick(@PathParam("connector") String connectorName) {
        return userConnectorService.deleteConfigOneClick(connectorName);
    }

    @PUT
    @Path("/{name}/config")
    public Uni<EventResponseModel> updateConfig(@PathParam("name") String name, UserConnectorConfigDto userConnectorConfigDto) {
        return userConnectorService.updateConnector(name, userConnectorConfigDto);
    }


    @GET
    @Path("/status/{connector}")
    public ResponseUtil getStatus(@PathParam("connector") String connector) {
        return userConnectorService.getStatus(connector);
    }

    @GET
    @Path("/oneclick/status/{connector}")
    public ResponseUtil getStatusOneClick(@PathParam("connector") String connector) {
        return userConnectorService.getStatusOneClick(connector);
    }

    @GET
    @Path("/topics/{connector}")
    public ResponseUtil getTopics(@PathParam("connector") String connector) {
        return userConnectorService.getTopics(connector);
    }

    @PUT
    @Path("/topics-reset/{connector}")
    public ResponseUtil resetTopics(@PathParam("connector") String connector) {
        return userConnectorService.resetTopics(connector);
    }

    @GET
    @Path("/all-connectors/{workspacename}")
    public ResponseUtil getAllConnectorByWorkSpaceId(@PathParam("workspacename") String workspacename) {
        ResponseUtil response = new ResponseUtil();
        List<UserConnectorConfigDto> byWorkSpaceId = userConnectorService.findminiAppName(workspacename);
        if (byWorkSpaceId != null && !byWorkSpaceId.isEmpty()) {
            response.setStatusCode(Status.OK.getStatusCode());
            response.setData(byWorkSpaceId);
            response.setMessage("data is present...");
            return response;
        }
        response.setStatusCode(NO_CONTENT.getStatusCode());
        response.setMessage("no data found");
        return response;
    }

    @GET
    @Path("/oneclick/all-connectors/{workspacename}")
    public ResponseUtil getAllConnectorByWorkSpaceIdOneClick(@PathParam("workspacename") String workspacename) {
        ResponseUtil response = new ResponseUtil();
        List<SourceSinkConnectorConfig> byWorkSpaceId = userConnectorService.findByWorkSpaceName(workspacename);
        if (byWorkSpaceId != null && !byWorkSpaceId.isEmpty()) {
            response.setStatusCode(Status.OK.getStatusCode());
            response.setData(byWorkSpaceId);
            response.setMessage("data is present...");
            return response;
        }
        response.setStatusCode(NO_CONTENT.getStatusCode());
        response.setMessage("no data found");
        return response;
    }

    @GET
    @Path("/all-connectors-state/{workspacename}")
    public ResponseUtil getAllConnectorStateByMiniAppName(@PathParam("workspacename") String workspacename) {
        ResponseUtil response = new ResponseUtil();
        Map<String, String> byWorkSpaceId = userConnectorService.findStatus(workspacename);
        if (byWorkSpaceId != null && !byWorkSpaceId.isEmpty()) {
            response.setStatusCode(Status.OK.getStatusCode());
            response.setData(byWorkSpaceId);
            response.setMessage("data is present...");
            return response;
        }
        response.setStatusCode(NO_CONTENT.getStatusCode());
        response.setMessage("no data found");
        return response;
    }

    @PUT
    @Path("/pause/{connector}")
    public ResponseUtil pauseConnector(@PathParam("connector") String connector) {
        return userConnectorService.pauseConnector(connector);
    }

    @PUT
    @Path("/resume/{connector}")
    public ResponseUtil resumeConnector(@PathParam("connector") String connector) {
        return userConnectorService.resumeConnector(connector);
    }

    @POST
    @Path("/table-structure")
    public ResponseUtil getTableStructure(UserConnectorConfigDto userConnectorConfigDto) {
        ResponseUtil response = new ResponseUtil();
        List<DataModelDTO> dataModelMapping = userConnectorService.getTableStructures(userConnectorConfigDto);
        if (dataModelMapping != null && !dataModelMapping.isEmpty()) {
            response.setData(dataModelMapping);
            response.setStatusCode(Status.OK.getStatusCode());
            return response;
        }
        response.setMessage("No data found for the given details..");
        response.setStatusCode(NO_CONTENT.getStatusCode());
        return response;
    }

    @GET
    @Path("/tables/{connectionName}")
    public List<String> getTables(@PathParam("connectionName") String connectionName) {
        ResponseUtil response = new ResponseUtil();
        List<String> tables = userConnectorService.getTables(connectionName);
        if (tables != null && !tables.isEmpty()) {
            return tables;
        }
        return tables;
    }

//    @GET
//    @Path("/topice")
//    public ResponseUtil getTopics() {
//        ResponseUtil response = new ResponseUtil();
//        userConnectorService.topicaAll();
//        response.setMessage("No data found for the given details..");
//        response.setStatusCode(NO_CONTENT.getStatusCode());
//        return response;
//    }
}
