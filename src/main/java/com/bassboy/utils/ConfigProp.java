package com.bassboy.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProp extends Properties{

    final static private String configPropFile = "src/main/resources/config.properties";
    private static ConfigProp configProp = null;

    private ConfigProp() throws IOException {}

    // static method to create instance of Singleton class
    public static ConfigProp getInstance() throws IOException {
        if (configProp == null)
            configProp = new ConfigProp();

        InputStream inStream = new FileInputStream(System.getProperty("user.dir") + "/" + configPropFile);
        configProp.load(inStream);

        return configProp;
    }

}
