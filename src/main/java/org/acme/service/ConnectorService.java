package org.acme.service;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.json.simple.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey="connector-api")
public interface ConnectorService {

    @POST
    @Path("/connectors")
    Response addConnector(JSONObject connector);

    @GET
    @Path("/connectors/{connector}/status")
    JSONObject findStatus(@PathParam("connector") String connector);

    @DELETE
    @Path("/connectors/{name}")
    Response deleteConnector(@PathParam("name") String name);

    @PUT
    @Path("/connectors/{name}/config")
    Response updateConnector(@PathParam("name") String name, JSONObject connector);

    @GET
    @Path("/connectors/{name}/config")
    JSONObject getConnector(@PathParam("name") String name);

    @PUT
    @Path("/connectors/{connector}/pause")
    void pauseConnector(@PathParam("connector") String connector);

    @PUT
    @Path("/connectors/{connector}/resume")
    void resumeConnector(@PathParam("connector") String connector);

    @GET
    @Path("/connectors/{connector}/topics")
    JSONObject getTopics(@PathParam("connector") String connector);

    @PUT
    @Path("/connectors/{connector}/topics/reset")
    JSONObject resetTopics(@PathParam("connector") String connector);

    @PUT
    @Path("/connector-plugins/{connector}/config/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    JSONObject validateConnector(@PathParam("connector") String connector, JSONObject connectorConfig);
}
