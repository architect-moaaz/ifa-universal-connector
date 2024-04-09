package org.acme.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataModelDTO {
    private String workspaceName;
    private String miniAppName;
    private String fileName;
    private String fileType;
    private List<DataModelProperty> dataModelProperties;
}
