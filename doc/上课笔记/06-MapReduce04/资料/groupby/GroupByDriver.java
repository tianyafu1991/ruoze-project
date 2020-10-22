package com.ruozedata.bigdata.mapreduce.groupby;

import com.ruozedata.bigdata.mapreduce.wc.WordCountMapper;
import com.ruozedata.bigdata.mapreduce.wc.WordCountReducer;
import com.ruozedata.bigdata.utils.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * 八股文编程
 **/
public class GroupByDriver {

    public static void main(String[] args) throws Exception {
        String input = "data/join/emp.txt";
        String output = "out";

        // 1 获取Job
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);

        FileUtils.deleteOutput(configuration, output);

        // 2 设置主类
        job.setJarByClass(GroupByDriver.class);

        // 3 设置Mapper和Reducer
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        // 4 设置Mapper阶段输出的key和value类型
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        // 5 设置Reduce阶段输出的key和value类型
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);

        // 6 设置输入和输出路径
        TextInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        // 7 提交Job
        boolean result = job.waitForCompletion(true);
        System.exit(result? 0:1 );
    }

    public static class MyReducer extends Reducer<IntWritable, IntWritable,IntWritable, IntWritable> {

        @Override
        protected void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

            int count = 0 ;

            for(IntWritable value : values) {
                count += value.get();
            }

            context.write(key, new IntWritable(count));

        }
    }


    public static class MyMapper extends Mapper<LongWritable, Text, IntWritable, IntWritable> {

        IntWritable ONE = new IntWritable(1);

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] splits = value.toString().split("\t");
            context.write(new IntWritable(Integer.parseInt(splits[7].trim())), ONE);
        }
    }
}
