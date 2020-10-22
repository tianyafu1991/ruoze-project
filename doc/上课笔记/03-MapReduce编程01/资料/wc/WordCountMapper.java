package com.ruozedata.bigdata.mapreduce.wc;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * KEYIN 输入数据KEY的数据类型
 * VALUEIN 输入数据VALUE的数据类型
 *
 * KEYOUT  输出数据KEY的数据类型
 * VALUEOUT 输出数据VALUE的数据类型
 **/
public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    IntWritable ONE = new IntWritable(1);

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        System.out.println("-----------setup----------");
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        System.out.println("-----------cleanup----------");
    }

    /**
     *
     * @param key   每一行数据的偏移量
     * @param value  每行数据的内容
     */
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        // TODO... 实现自己的业务逻辑

        // 获取每一行的数据，按照指定分隔符进行拆分
        String[] splits = value.toString().split(",");

        // 输出
        for(String word : splits) {
            context.write(new Text(word), ONE);
        }

    }
}
