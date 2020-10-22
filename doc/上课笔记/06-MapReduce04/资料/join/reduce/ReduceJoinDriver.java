package com.ruozedata.bigdata.mapreduce.join.reduce;

import com.ruozedata.bigdata.mapreduce.ser.Access;
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
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 八股文编程
 **/
public class ReduceJoinDriver {

    public static void main(String[] args) throws Exception {
        String input = "data/join";
        String output = "out";

        // 1 获取Job
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);

        FileUtils.deleteOutput(configuration, output);

        // 2 设置主类
        job.setJarByClass(ReduceJoinDriver.class);

        // 3 设置Mapper和Reducer
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        // 4 设置Mapper阶段输出的key和value类型
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Info.class);

        // 5 设置Reduce阶段输出的key和value类型
        job.setOutputKeyClass(Info.class);
        job.setOutputValueClass(NullWritable.class);

        // 6 设置输入和输出路径
        TextInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        // 7 提交Job
        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);
    }

    public static class MyMapper extends Mapper<LongWritable, Text, IntWritable, Info> {

        String name ;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            FileSplit fileSplit = (FileSplit)context.getInputSplit();
            name = fileSplit.getPath().getName();
            System.out.println(name);
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] splits = value.toString().split("\t");
            if(name.contains("emp")) {  // from emp
                int empno = Integer.parseInt(splits[0].trim());
                String ename = splits[1].trim();
                int deptno = Integer.parseInt(splits[7].trim());

                Info info = new Info();
                info.setEmpno(empno);
                info.setEname(ename);
                info.setDeptno(deptno);
                info.setDname("");
                info.setFlag(1);
                context.write(new IntWritable(deptno), info);
            } else { // from dept
                int deptno = Integer.parseInt(splits[0].trim());
                String dname = splits[1].trim();
                Info info = new Info();
                info.setEmpno(0);
                info.setEname("");
                info.setDeptno(deptno);
                info.setDname(dname);
                info.setFlag(2);
                context.write(new IntWritable(deptno), info);
            }
        }
    }

    public static class MyReducer extends Reducer<IntWritable, Info, Info, NullWritable> {

        // 相同的deptno(key)所对应的emp 和 dept的数据都落在了values
        @Override
        protected void reduce(IntWritable key, Iterable<Info> values, Context context) throws IOException, InterruptedException {

            List<Info> emps = new ArrayList<>();

            String dname = "";

            for(Info info : values) {
                if(info.getFlag() == 1) { //emp
                    Info tmp = new Info();
                    tmp.setEmpno(info.getEmpno());
                    tmp.setEname(info.getEname());
                    tmp.setDeptno(info.getDeptno());
                    emps.add(tmp);
                } else {  // dept
                    dname = info.getDname();
                }
            }

            for(Info bean : emps) {
                bean.setDname(dname);
                context.write(bean, NullWritable.get());
            }
        }
    }
}
