package org.acme.resource.utili;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponseModel {
    private String message;
    private Object data;
    private int status;
}
