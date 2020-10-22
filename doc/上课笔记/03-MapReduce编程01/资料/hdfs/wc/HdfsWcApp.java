package com.ruozedata.bigdata.hdfs.wc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;

/**
 * @author PK哥
 **/
public class HdfsWcApp {

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.set("dfs.client.use.datanode.hostname","true");
        conf.set("dfs.replication","1");
        URI uri = new URI("hdfs://ruozedata001:8020");
        FileSystem fileSystem = FileSystem.get(uri, conf, "hadoop");

        RuozedataMapper mapper = new WordCountMapper();
        RuozedataContext context = new RuozedataContext();


        Path input = new Path("/hdfswc/input/");
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
        Path output = new Path("/hdfsapi/wc/out");
        FSDataOutputStream out = fileSystem.create(new Path(output, new Path("wc.out")));
        for(Map.Entry<Object,Object> entry :  cacheMap.entrySet()) {
            System.out.println(entry.getKey() + "..." + entry.getValue());
            out.write((entry.getKey() + "\t" + entry.getValue() + "\n").getBytes());
        }

        out.close();
        fileSystem.close();

        System.out.println("PK哥wc统计完毕...");

    }
}
