package com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.add;

import java.io.File;

public class AddDenpendies {

    public static void main(String[] args) {
        String template = "ln -s /opt/cloudera/parcels/CDH/jars/%s $AZKABAN_EXECUTOR_HOME/extlib/%s";
        String path = "/opt/cloudera/parcels/CDH/jars";
        File file = new File(path);
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for (File subFile : files) {
                if(subFile.getName().endsWith(".jar")){
                    String name = subFile.getName();
                    String cmd = String.format(template, name, name);
                    System.out.println(cmd);
                }
            }

        }
    }
}
