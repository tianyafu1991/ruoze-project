package com.ruozedata.bigdata.mapreduce.inputformat;

import com.ruozedata.bigdata.utils.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @author PK哥
 **/
public class MySQLDBInputFormatDriver {

    public static void main(String[] args) throws Exception {
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
        job.setJarByClass(MySQLDBInputFormatDriver.class);

        // 3 设置Mapper
        job.setMapperClass(MyMapper.class);

        // 4 设置Mapper阶段输出的key和value类型
        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(DeptWritable.class);


        // 6 设置输入和输出路径
        String[] fields = new String[]{"deptno","dname","loc"};
        DBInputFormat.setInput(job, DeptWritable.class,"dept",null,null,fields);

        FileOutputFormat.setOutputPath(job, new Path(output));

        // 7 提交Job
        boolean result = job.waitForCompletion(true);
        System.exit(result? 0:1 );
    }


    public static class MyMapper extends Mapper<LongWritable, DeptWritable, NullWritable, DeptWritable> {
        @Override
        protected void map(LongWritable key, DeptWritable value, Context context) throws IOException, InterruptedException {
            context.write(NullWritable.get(), value);
        }
    }

}
