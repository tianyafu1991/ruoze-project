package com.ruozedata.bigdata.hdfs.wc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

/**
 * @author PK哥
 **/
public class HdfsWcApp02 {

    public static void main(String[] args) throws Exception {

        Properties properties = ParamsUtils.getProperties();

        Configuration conf = new Configuration();
        conf.set("dfs.client.use.datanode.hostname","true");
        conf.set("dfs.replication","1");
        URI uri = new URI(properties.getProperty(Constants.HDFS_URI));
        FileSystem fileSystem = FileSystem.get(uri, conf, "hadoop");


        String className = properties.getProperty(Constants.MAPPER_CLASS);
        Class<?> clazz = Class.forName(className);
        RuozedataMapper mapper =  (RuozedataMapper)clazz.newInstance();
        RuozedataContext context = new RuozedataContext();

        Path input = new Path(properties.getProperty(Constants.INPUT_PATH));
        RemoteIterator<LocatedFileStatus> iterator = fileSystem.listFiles(input, false);
        while(iterator.hasNext()) {
            LocatedFileStatus file = iterator.next();
            FSDataInputStream in = fileSystem.open(file.getPath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while((line = reader.readLine()) != null) {
                // System.out.println(line);
                mapper.map(line, context);
            }
            reader.close();
            in.close();
        }

        Map<Object, Object> cacheMap = context.getCacheMap();
//
        // path = parent + child  ==  /hdfsapi/wc/out/wc.out
        Path output = new Path(properties.getProperty(Constants.OUTPUT_PATH));
        FSDataOutputStream out = fileSystem.create(new Path(output, new Path(properties.getProperty(Constants.OUTPUT_FILE))));
        for(Map.Entry<Object,Object> entry :  cacheMap.entrySet()) {
            System.out.println(entry.getKey() + "..." + entry.getValue());
            out.write((entry.getKey() + "\t" + entry.getValue() + "\n").getBytes());
        }

        out.close();
        fileSystem.close();

        System.out.println("PK哥wc统计完毕...");

    }
}
