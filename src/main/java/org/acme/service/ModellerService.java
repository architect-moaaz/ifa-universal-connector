package org.acme.service;

import io.smallrye.mutiny.Uni;
import org.acme.model.DataModelDTO;
import org.acme.resource.utili.EventResponseModel;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "modeller-api")
public interface ModellerService {
    @POST
    @Path("/modellerService/datamodeller/createFile")
    Uni<EventResponseModel> addModeller(DataModelDTO dataModelDTO);

    @GET
    @Path("/modellerService/listWorkspaces")
    Uni<RestResponse<EventResponseModel>> listWorkspaces();
}
