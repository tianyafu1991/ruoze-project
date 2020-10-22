package com.ruozedata.bigdata.mapreduce.wc;

import com.ruozedata.bigdata.utils.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 八股文编程
 **/
public class WordCountDriverV2 {

    public static void main(String[] args) throws Exception {

        String input = args[0]; //"data/ruozedata.txt";
        String output = args[1] ; // "out";



        // 1 获取Job
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);


        FileUtils.deleteOutput(configuration, output);

        // 2 设置主类
        job.setJarByClass(WordCountDriverV2.class);

        // 3 设置Mapper和Reducer
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);

        // 4 设置Mapper阶段输出的key和value类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        // 5 设置Reduce阶段输出的key和value类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 6 设置输入和输出路径
        FileInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        // 7 提交Job
        boolean result = job.waitForCompletion(true);
        System.exit(result? 0:1 );
    }
}
