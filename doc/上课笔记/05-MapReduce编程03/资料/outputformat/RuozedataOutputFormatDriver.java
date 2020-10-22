package com.ruozedata.bigdata.mapreduce.outputformat;

import com.ruozedata.bigdata.mapreduce.ser.Access;
import com.ruozedata.bigdata.mapreduce.wc.WordCountDriver;
import com.ruozedata.bigdata.mapreduce.wc.WordCountMapper;
import com.ruozedata.bigdata.mapreduce.wc.WordCountReducer;
import com.ruozedata.bigdata.utils.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @author PK哥
 **/
public class RuozedataOutputFormatDriver {

    public static void main(String[] args) throws Exception {
        String input = "data/outputformat.data";
        String output = "out";

        // 1 获取Job
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);

        FileUtils.deleteOutput(configuration, output);

        // 2 设置主类
        job.setJarByClass(RuozedataOutputFormatDriver.class);

        // 3 设置Mapper和Reducer
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        // 4 设置Mapper阶段输出的key和value类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);

        // 5 设置Reduce阶段输出的key和value类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);


        job.setOutputFormatClass(RuozeOutputFormat.class);

        // 6 设置输入和输出路径
        TextInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        // 7 提交Job
        boolean result = job.waitForCompletion(true);
        System.exit(result? 0:1 );
    }


    public static class MyMapper extends Mapper<LongWritable, Text, Text, NullWritable> {

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            context.write(value, NullWritable.get());
        }
    }

    public static class MyReducer extends Reducer<Text, NullWritable, Text, NullWritable> {
        @Override
        protected void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            for(NullWritable value : values) {
                context.write(new Text(key.toString()+"\n"), NullWritable.get());
            }
        }
    }

    public static class RuozeOutputFormat extends FileOutputFormat<Text, NullWritable> {

        @Override
        public RecordWriter<Text, NullWritable> getRecordWriter(TaskAttemptContext job) throws IOException, InterruptedException {
            return new RuozeRecordWriter(job);
        }
    }

    public static class RuozeRecordWriter extends RecordWriter<Text, NullWritable> {

        FileSystem fileSystem = null;
        FSDataOutputStream ruozedataOut = null;
        FSDataOutputStream otherOut = null;

        public RuozeRecordWriter(TaskAttemptContext job){
            try {
                fileSystem = FileSystem.get(job.getConfiguration());
                ruozedataOut = fileSystem.create(new Path("out/ruozedata.log"));
                otherOut = fileSystem.create(new Path("out/other.log"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void write(Text key, NullWritable value) throws IOException, InterruptedException {
            if(key.toString().contains("ruozedata.com")) {
                ruozedataOut.write(key.toString().getBytes());
            } else {
                otherOut.write(key.toString().getBytes());
            }
        }

        @Override
        public void close(TaskAttemptContext context) throws IOException, InterruptedException {
            IOUtils.closeStream(ruozedataOut);
            IOUtils.closeStream(otherOut);
            fileSystem.close();
        }
    }
}
