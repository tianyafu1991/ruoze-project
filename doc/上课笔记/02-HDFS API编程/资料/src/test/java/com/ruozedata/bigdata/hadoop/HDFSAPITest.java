package com.ruozedata.bigdata.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import java.net.URI;

/**
 * HDFS API测试用例
 *
 * 编程：入口点
 **/
public class HDFSAPITest {

    @Test
    public void mkdir() throws Exception {
        // 此时的conf并没有与我们的HDFS关联上
        Configuration conf = new Configuration();

        // 获取HDFS客户端对象
        FileSystem fileSystem = FileSystem.get(conf);

        // 执行业务逻辑
        Path path = new Path("/hdfsapi");
        fileSystem.mkdirs(path);

        // 释放资源
        fileSystem.close();
    }

    @Test
    public void mkdir02() throws Exception {
        Configuration conf = new Configuration();

        URI uri = new URI("hdfs://ruozedata001:8020");

        // 获取HDFS客户端对象
        FileSystem fileSystem = FileSystem.get(uri, conf, "hadoop");

        // 执行业务逻辑
        Path path = new Path("/hdfsapi2");
        fileSystem.mkdirs(path);

        // 释放资源
        fileSystem.close();
    }
}
