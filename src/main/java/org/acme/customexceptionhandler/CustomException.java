package org.acme.customexceptionhandler;

import javax.ws.rs.core.Response;
import java.io.Serializable;

public class CustomException extends
        Exception implements Serializable {

    private static final long serialVersionUID = 1L;
    private Response response;

    public CustomException(Response response){
        this.response = response;
    }

    public CustomException(String message) {
        super(message);
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomException(Throwable cause) {
        super(cause);
    }

    public CustomException(String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
