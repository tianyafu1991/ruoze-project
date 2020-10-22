package com.ruozedata.bigdata.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;

/**
 * 使用IO的方式
 */
public class HDFSAPITest03 {

    FileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        Configuration conf = new Configuration();
        conf.set("dfs.client.use.datanode.hostname","true");
        conf.set("dfs.replication","1");
        URI uri = new URI("hdfs://ruozedata001:8020");
        fileSystem = FileSystem.get(uri, conf, "hadoop");
    }

    @Test
    public void copyFromLocalFile() throws Exception {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File("data/ruozedata.txt")));
        FSDataOutputStream out = fileSystem.create(new Path("/hdfsapi/pk/io.txt"));

        // in ==> out
        IOUtils.copyBytes(in, out, 4096);

        IOUtils.closeStream(in);
        IOUtils.closeStream(out);
    }

    @Test
    public void copyToLocalFile() throws Exception {
        FSDataInputStream in = fileSystem.open(new Path("/hdfsapi/pk/io.txt"));
        FileOutputStream out = new FileOutputStream(new File("out/io-out.txt"));

        IOUtils.copyBytes(in, out, fileSystem.getConf());

        IOUtils.closeStream(in);
        IOUtils.closeStream(out);
    }


    @Test
    public void download01() throws Exception {
        FSDataInputStream in = fileSystem.open(new Path("/hdfsapi/spark-2.4.5-bin-2.6.0-cdh5.16.2.tgz"));
        FileOutputStream out = new FileOutputStream(new File("out/spark.tgz.part0"));
        // 0 -128M
        byte[] buffer = new byte[2048];
        for(int i=0; i<1024*128*1024; i++) {
            in.read(buffer);
            out.write(buffer);
        }

        IOUtils.closeStream(in);
        IOUtils.closeStream(out);
    }


    @Test
    public void download02() throws Exception {
        FSDataInputStream in = fileSystem.open(new Path("/hdfsapi/spark-2.4.5-bin-2.6.0-cdh5.16.2.tgz"));
        FileOutputStream out = new FileOutputStream(new File("out/spark.tgz.part1"));

        // seek 128~
        in.seek(1024*1024*128);
        IOUtils.copyBytes(in, out, fileSystem.getConf());

        IOUtils.closeStream(in);
        IOUtils.closeStream(out);
    }
}
