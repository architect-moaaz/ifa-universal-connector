package org.acme.resource.utili;

import java.util.Locale;

public class Utility {
    public static String createFileName(String fileName){
        int length = fileName.length();
        fileName = fileName.toUpperCase();
        StringBuilder result = new StringBuilder(fileName);
        String s = fileName.substring(1, length).toLowerCase();
        return result.replace(1,length,s).toString();
    }
}
