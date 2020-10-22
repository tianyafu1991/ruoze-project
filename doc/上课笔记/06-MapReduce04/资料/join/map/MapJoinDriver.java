package com.ruozedata.bigdata.mapreduce.join.map;

import com.ruozedata.bigdata.mapreduce.join.reduce.Info;
import com.ruozedata.bigdata.utils.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 八股文编程
 **/
public class MapJoinDriver {

    public static void main(String[] args) throws Exception {
        String input = "data/join/emp.txt";
        String output = "out";

        // 1 获取Job
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);

        FileUtils.deleteOutput(configuration, output);

        // 2 设置主类
        job.setJarByClass(MapJoinDriver.class);

        // 3 设置Mapper和Reducer
        job.setMapperClass(MyMapper.class);
        job.setNumReduceTasks(0);

        // 4 设置Mapper阶段输出的key和value类型
        job.setMapOutputKeyClass(Info.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.addCacheFile(new URI("data/join/dept.txt"));

        // 6 设置输入和输出路径
        TextInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        // 7 提交Job
        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);
    }

    public static class MyMapper extends Mapper<LongWritable, Text, Info, NullWritable> {

        String name ;

        Map<String,String> cache = new HashMap<>();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {

            URI[] cacheFiles = context.getCacheFiles();
            String path = cacheFiles[0].getPath().toString();

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String line = "";
            while(StringUtils.isNotEmpty(line=reader.readLine())) {
                String[] splits = line.split("\t");
                cache.put(splits[0], splits[1]);
            }
            IOUtils.closeStream(reader);
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] splits = value.toString().split("\t");
            int empno = Integer.parseInt(splits[0].trim());
            String ename = splits[1].trim();
            int deptno = Integer.parseInt(splits[7].trim());

            Info info = new Info();
            info.setEmpno(empno);
            info.setEname(ename);
            info.setDeptno(deptno);
            info.setDname(cache.get(deptno+""));
            context.write(info, NullWritable.get());
        }
    }
}
