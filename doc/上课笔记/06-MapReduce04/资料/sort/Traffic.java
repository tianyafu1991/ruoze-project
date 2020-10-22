package com.ruozedata.bigdata.mapreduce.sort;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author PK哥
 **/
public class Traffic implements WritableComparable<Traffic> {

    private long up;
    private long down;
    private long sum;

    public Traffic(){}

    public Traffic(long up, long down, long sum) {
        this.up = up;
        this.down = down;
        this.sum = sum;
    }

    // 实现自定义排序规则
    @Override
    public int compareTo(Traffic o) {
        return this.getSum() > o.getSum() ? -1 : 1 ;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(up);
        out.writeLong(down);
        out.writeLong(sum);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.up = in.readLong();
        this.down = in.readLong();
        this.sum = in.readLong();
    }

    @Override
    public String toString() {
        return up + "\t" + down + "\t" + sum;
    }

    public long getUp() {
        return up;
    }

    public void setUp(long up) {
        this.up = up;
    }

    public long getDown() {
        return down;
    }

    public void setDown(long down) {
        this.down = down;
    }

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }
}
