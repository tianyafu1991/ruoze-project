package com.ruozedata.bigdata.mapreduce.partitioner;

import com.ruozedata.bigdata.mapreduce.ser.Access;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * @author PK哥
 **/
public class PhonePartitioner extends Partitioner<Text, Access> {

    @Override
    public int getPartition(Text text, Access access, int numPartitions) {
        String phone = text.toString();
        if(phone.startsWith("13")) {
            return 0;
        } else if(phone.startsWith("15")) {
            return 1;
        } else {
            return 2;
        }
    }
}
