package com.ruozedata.bigdata.mapreduce.outputformat;

import com.ruozedata.bigdata.mapreduce.inputformat.DeptWritable;
import com.ruozedata.bigdata.utils.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBInputFormat;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @author PK哥
 **/
public class MySQLDBOutputFormatDriver {


    public static void main(String[] args) throws Exception {
        String input = "data/mysqlout.txt";
        String output = "out";

        // 1 获取Job
        Configuration configuration = new Configuration();

        DBConfiguration.configureDB(configuration,
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://ruozedata001:3306/ruozedata",
                "root",
                "!Ruozedata123");

        Job job = Job.getInstance(configuration);

        FileUtils.deleteOutput(configuration, output);

        // 2 设置主类
        job.setJarByClass(MySQLDBOutputFormatDriver.class);

        // 3 设置Mapper
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        // 4 设置Mapper阶段输出的key和value类型
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        // 5 设置Reduce阶段输出的key和value类型
        job.setOutputKeyClass(DeptWritable.class);
        job.setOutputValueClass(NullWritable.class);


        // 6 设置输入和输出路径
        FileInputFormat.setInputPaths(job, new Path(input));
        job.setOutputFormatClass(DBOutputFormat.class);
        DBOutputFormat.setOutput(job, "dept", "deptno","dname","loc");

        // 7 提交Job
        boolean result = job.waitForCompletion(true);
        System.exit(result? 0:1 );
    }


    public static class MyMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            context.write(key, value);
        }
    }

    public static class MyReducer extends Reducer<LongWritable, Text, DeptWritable, NullWritable> {
        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for(Text value : values) {
                String[] splits = value.toString().split(",");
                DeptWritable dept = new DeptWritable();
                dept.setDeptno(Integer.parseInt(splits[0]));
                dept.setDname(splits[1]);
                dept.setLoc(splits[2]);
                context.write(dept, NullWritable.get());
            }
        }
    }
}
