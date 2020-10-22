package com.ruozedata.bigdata.mapreduce.sort;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author PK哥
 **/
public class Order implements WritableComparable<Order> {

    private int id;
    private double price;

    public Order(){}

    public Order(int id, double price) {
        this.id = id;
        this.price = price;
    }

    @Override
    public String toString() {
        return id + "\t" + price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // id asc, price desc
    @Override
    public int compareTo(Order o) {
        int result = 0;
        if(this.getId() > o.getId()) {
            result = 1;
        } else if(this.getId() < o.getId()) {
            result = -1;
        } else {
            if(this.getPrice() > o.getPrice()) {
                result = -1;
            } else if(this.getPrice() < o.getPrice()) {
                result = 1;
            } else {
                result = 0;
            }
        }

        return result;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(id);
        out.writeDouble(price);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.id = in.readInt();
        this.price = in.readDouble();
    }
}
