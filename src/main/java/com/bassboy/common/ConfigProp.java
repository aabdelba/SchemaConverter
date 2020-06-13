package com.bassboy.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProp extends Properties{

    private static ConfigProp configProp = null;

    private ConfigProp() throws IOException {}

    // static method to create instance of Singleton class
    public static ConfigProp getInstance() throws IOException {
        if (configProp == null)
            configProp = new ConfigProp();

        // this is hardcoded. bad
//        InputStream inStream = new FileInputStream(System.getProperty("user.dir") + "/" + configPropFile);

        InputStream inStream = configProp.getClass().getClassLoader().getResourceAsStream("config.properties");

        configProp.load(inStream);

        return configProp;
    }

    public static void main(String[] args) throws IOException {
        configProp.getInstance();
    }



}
