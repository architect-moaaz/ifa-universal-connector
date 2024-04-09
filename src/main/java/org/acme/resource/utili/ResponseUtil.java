package org.acme.resource.utili;

import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.customexceptionhandler.CustomException;
import org.jboss.resteasy.reactive.RestResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseUtil{
    private String message;
    private Object data;
    private int statusCode;


    public static Uni<RestResponse<EventResponseModel>> validateResponse(Uni<EventResponseModel> responseUni){
        return responseUni.onItem().transform(
                item -> RestResponse.ResponseBuilder.create(Status.OK, item).build()
        ).onFailure().recoverWithItem(
                call -> {
                    EventResponseModel model = new EventResponseModel();
                    CustomException exception = (CustomException) call;
                    model.setMessage(exception.getResponse().getHeaderString("Error"));
                    return RestResponse.ResponseBuilder.create(exception.getResponse().getStatusInfo(), model).
                            type(MediaType.APPLICATION_JSON).
                            build();
                }
        );
    }
}
