package com.ruozedata.bigdata.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

/**
 * HDFS API测试用例
 *
 * 编程：入口点
 **/
public class HDFSAPITest02 {

    FileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        Configuration conf = new Configuration();
        conf.set("dfs.client.use.datanode.hostname","true");
        conf.set("dfs.replication","1");
        URI uri = new URI("hdfs://ruozedata001:8020");
        fileSystem = FileSystem.get(uri, conf, "hadoop");
    }

    @After
    public void tearDown() throws Exception {
        if(null != fileSystem) {
            fileSystem.close();
        }
    }

    @Test
    public void mkdir() throws Exception {
        Path path = new Path("/hdfsapi/pk");
        fileSystem.mkdirs(path);
    }

    @Test
    public void copyFromLocalFile() throws Exception {
        Path src = new Path("data/ruozedata.txt");
        Path dst = new Path("/hdfsapi/pk");
        fileSystem.copyFromLocalFile(src, dst);
    }

    @Test
    public void testReplication() throws Exception {
        System.out.println(fileSystem.getConf().get("dfs.replication"));
    }

    /**
     *  完成从HDFS拷贝数据到本地
     */
    @Test
    public void copyToLocalFile() throws Exception {
        Path src = new Path("/hdfsapi/pk/ruozedata.txt");
        Path dst = new Path("out/ruozedata-3.txt");
        fileSystem.copyToLocalFile(true, src, dst);
    }

    @Test
    public void rename() throws Exception {
        Path src = new Path("/hdfsapi/pk/ruozedata.txt");
        Path dst = new Path("/hdfsapi/pk/ruozedata-2.txt");
        fileSystem.rename(src, dst);
    }

    @Test
    public void listFiles() throws Exception {
        RemoteIterator<LocatedFileStatus> files = fileSystem.listFiles(new Path("/hdfsapi/pk"), true);
        while (files.hasNext()) {
            LocatedFileStatus fileStatus = files.next();
            String isDir = fileStatus.isDirectory() ? "文件夹" : "文件";
            String permission = fileStatus.getPermission().toString();
            short replication = fileStatus.getReplication();
            long length = fileStatus.getLen();
            String path = fileStatus.getPath().toString();

            System.out.println(isDir + "\t" + permission+ "\t" +replication
                    + "\t" + length + "\t" + path);


            BlockLocation[] blockLocations = fileStatus.getBlockLocations();
            for(BlockLocation blockLocation : blockLocations) {
                String[] hosts = blockLocation.getHosts();
                for(String host : hosts) {
                    System.out.println(host);
                }
            }
        }
    }

    @Test
    public void delete() throws Exception {
        fileSystem.delete(new Path("/hdfsapi/pk"), true);
    }


}
