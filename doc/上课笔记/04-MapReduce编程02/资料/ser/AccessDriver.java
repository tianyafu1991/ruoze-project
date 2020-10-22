package com.ruozedata.bigdata.mapreduce.ser;

import com.ruozedata.bigdata.mapreduce.wc.WordCountDriver;
import com.ruozedata.bigdata.mapreduce.wc.WordCountMapper;
import com.ruozedata.bigdata.mapreduce.wc.WordCountReducer;
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
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @author PK哥
 **/
public class AccessDriver {

    public static void main(String[] args) throws Exception {
        String input = "data/access.log";
        String output = "out";

        // 1 获取Job
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);

        FileUtils.deleteOutput(configuration, output);

        // 2 设置主类
        job.setJarByClass(AccessDriver.class);

        // 3 设置Mapper和Reducer
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        // 4 设置Mapper阶段输出的key和value类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Access.class);

        // 5 设置Reduce阶段输出的key和value类型
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Access.class);

        // 6 设置输入和输出路径
        FileInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        // 7 提交Job
        boolean result = job.waitForCompletion(true);
        System.exit(result? 0:1 );
    }



    public static class MyMapper extends Mapper<LongWritable, Text, Text, Access> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            String[] splits = value.toString().split("\t");
            String phone = splits[1]; // index from 0
            long up = Long.parseLong(splits[splits.length - 3]);
            long down = Long.parseLong(splits[splits.length - 2]);

            context.write(new Text(phone), new Access(phone, up, down));
        }
    }

    public static class MyReducer extends Reducer<Text, Access, NullWritable, Access> {

        @Override
        protected void reduce(Text phone, Iterable<Access> values, Context context) throws IOException, InterruptedException {

            long ups = 0L;
            long downs = 0L;

            for(Access access : values) {
                ups += access.getUp();
                downs += access.getDown();
            }

            context.write(NullWritable.get(), new Access(phone.toString(), ups, downs));
        }
    }
}
