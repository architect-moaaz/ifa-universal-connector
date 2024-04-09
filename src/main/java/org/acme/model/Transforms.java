package org.acme.model;

import lombok.Data;

import java.util.HashMap;

@Data
public class Transforms {
    private HashMap<String,String> renameFieldsNames;
}
