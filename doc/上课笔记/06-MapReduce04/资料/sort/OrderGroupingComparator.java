package com.ruozedata.bigdata.mapreduce.sort;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * @author PK哥
 **/
public class OrderGroupingComparator extends WritableComparator {

    // 一定要使用构造方法去调用父类的构造方法进行初始化
    public OrderGroupingComparator(){
        super(Order.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        Order order1 = (Order)a;
        Order order2 = (Order)b;

        int result;

        if(order1.getId() > order2.getId()) {
            result = 1;
        } else if(order1.getId() < order2.getId()) {
            result = -1;
        } else {
            result = 0;
        }

        return result;
    }
}
