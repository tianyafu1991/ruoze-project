package com.ruozedata.bigdata.mapreduce.inputformat;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.db.DBWritable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author PK哥
 **/
public class DeptWritable implements Writable, DBWritable {

    private int deptno;
    private String dname;
    private String loc;

    public DeptWritable(){}

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(deptno);
        out.writeUTF(dname);
        out.writeUTF(loc);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.deptno = in.readInt();
        this.dname = in.readUTF();
        this.loc = in.readUTF();
    }

    @Override
    public void write(PreparedStatement statement) throws SQLException {
        statement.setInt(1, deptno);
        statement.setString(2, dname);
        statement.setString(3, loc);
    }

    @Override
    public void readFields(ResultSet resultSet) throws SQLException {
        this.deptno = resultSet.getInt(1);
        this.dname = resultSet.getString(2);
        this.loc = resultSet.getString(3);
    }

    @Override
    public String toString() {
        return deptno + "\t" + dname + "\t" + loc;
    }

    public int getDeptno() {
        return deptno;
    }

    public void setDeptno(int deptno) {
        this.deptno = deptno;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }
}
