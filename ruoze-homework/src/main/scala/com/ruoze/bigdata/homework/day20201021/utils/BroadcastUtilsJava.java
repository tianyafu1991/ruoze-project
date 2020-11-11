package com.ruoze.bigdata.homework.day20201021.utils;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BroadcastUtilsJava {

    private static Long lastUpdateTimeStamp = 0L;

    private Broadcast<List<String>> broadcastList ;

    private static String url = "jdbc:mysql://hadoop:3306/ruozedata?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";

    private static String user = "root";
    private static String password = "root";
    private static String driver = "com.mysql.jdbc.Driver";
    private static String dbTable = "prewarning_config";

    private static BroadcastUtilsJava broadcastUtilsJava = new BroadcastUtilsJava();

    public static BroadcastUtilsJava getInstance(){
        return broadcastUtilsJava;
    }

    private BroadcastUtilsJava(){
    }

    public Broadcast<List<String>> getOrUpdateBroadcastList(SparkSession spark,Broadcast<List<String>> broadcasts){
        long now = new Date().getTime();
        if(broadcasts == null || (now - lastUpdateTimeStamp) > 60000){
            if(null != broadcasts){
                broadcasts.unpersist();
            }

            lastUpdateTimeStamp = new Date().getTime();
            Dataset<Row> keywordsDF = spark.read().format("jdbc")
                    .option(JDBCOptions.JDBC_URL(), url)
                    .option(JDBCOptions.JDBC_DRIVER_CLASS(), driver)
                    .option(JDBCOptions.JDBC_TABLE_NAME(), dbTable)
                    .option("user", user)
                    .option("password", password)
                    .load();
            List<Row> rows = keywordsDF.collectAsList();
            List<String> keywords = new ArrayList<>();
            for (Row row : rows) {
                keywords.add(row.<String>getAs("keywords"));
            }

            return JavaSparkContext.fromSparkContext(spark.sparkContext()).broadcast(keywords);
        }else {
            return broadcasts;
        }
    }


}
