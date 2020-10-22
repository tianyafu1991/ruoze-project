package com.ruozedata.bigdata.mapreduce.wc;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * KEYIN 输入数据KEY的数据类型
 * VALUEIN 输入数据VALUE的数据类型
 *
 * KEYOUT  输出数据KEY的数据类型
 * VALUEOUT 输出数据VALUE的数据类型
 **/
public class WordCountReducer extends Reducer<Text, IntWritable,Text, IntWritable> {

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

        int count = 0 ;

        for(IntWritable value : values) {
            count += value.get();
        }

        context.write(key, new IntWritable(count));

    }
}
