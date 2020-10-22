package com.ruozedata.bigdata.hdfs.wc;

import java.io.IOException;
import java.util.Properties;

/**
 * @author PK哥
 **/
public class ParamsUtils {

    private static Properties properties = new Properties();

    static {
        try {
            properties.load(ParamsUtils.class.getClassLoader().getResourceAsStream("wc.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static void main(String[] args) {
        System.out.println(getProperties().getProperty("HDFS_URI"));
    }

}
