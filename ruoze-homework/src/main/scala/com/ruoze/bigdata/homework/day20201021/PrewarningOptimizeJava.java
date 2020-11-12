package com.ruoze.bigdata.homework.day20201021;

import com.ruoze.bigdata.homework.day20201018.CDHLogJava;
import com.ruoze.bigdata.homework.day20201021.utils.BroadcastUtilsJava;
import com.ruoze.bigdata.homework.day20201021.utils.InfluxDBUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.json.JSONObject;

import java.util.*;

public class PrewarningOptimizeJava {

    private static Broadcast<List<String>> broadcastList;

    public static void main(String[] args) {
        try {
//            System.setProperty("HADOOP_USER_NAME", "hadoop");
            /*String serverURL = "http://" + InfluxDBUtils.getInfluxIP() + ":" + InfluxDBUtils.getInfluxPORT(true);
            String username = "admin";
            String password = "admin";
            InfluxDB influxDB = InfluxDBFactory.connect(serverURL, username, password);
            String retentionPolicy = InfluxDBUtils.defaultRetentionPolicy(influxDB.version());*/
            SparkConf conf = new SparkConf();
            conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
            conf.registerKryoClasses(new Class[]{ConsumerRecord.class});
            SparkSession spark = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate();
            JavaStreamingContext jssc = new JavaStreamingContext(new JavaSparkContext(spark.sparkContext()), new Duration(5000));

            Map<String, Object> kafkaParams = new HashMap<>();
            kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop01:9092");

            //
            kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "ruozedata_java");
            kafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            String[] topics = new String[]{"PREWARNING"};
            JavaInputDStream<ConsumerRecord<String, String>> lines = KafkaUtils.createDirectStream(jssc,
                    LocationStrategies.PreferConsistent(),
                    ConsumerStrategies.Subscribe(Arrays.asList(topics), kafkaParams));


            JavaDStream<CDHLogJava> cdhLogDStream = lines.filter(new Function<ConsumerRecord<String, String>, Boolean>() {
                @Override
                public Boolean call(ConsumerRecord<String, String> record) throws Exception {
                    String value = record.value();
                    return value.contains("INFO") || value.contains("ERROR") || value.contains("WARNING") || value.contains("DEBUG") || value.contains("FATAL");
                }
            }).map(new Function<ConsumerRecord<String, String>, CDHLogJava>() {
                @Override
                public CDHLogJava call(ConsumerRecord<String, String> record) throws Exception {
                    CDHLogJava cdhLogJava = new CDHLogJava();
                    String value = record.value();
                    JSONObject jsonObject = new JSONObject(value);
                    cdhLogJava.setHostname(jsonObject.getString("hostname"));
                    cdhLogJava.setServicename(jsonObject.getString("servicename"));
                    cdhLogJava.setTime(jsonObject.getString("time"));
                    cdhLogJava.setLogtype(jsonObject.getString("logtype"));
                    cdhLogJava.setLoginfo(jsonObject.getString("loginfo"));
                    return cdhLogJava;
                }
            });

            JavaDStream<CDHLogJava> windowedDStream = cdhLogDStream.window(new Duration(5000), new Duration(5000));

            windowedDStream.foreachRDD(new VoidFunction<JavaRDD<CDHLogJava>>() {
                @Override
                public void call(JavaRDD<CDHLogJava> cdhLogJavaJavaRDD) throws Exception {
                    if (cdhLogJavaJavaRDD.isEmpty()) {
                        System.out.println("========没有数据=================");
                    } else {
                        Dataset<Row> df = spark.createDataFrame(cdhLogJavaJavaRDD, CDHLogJava.class);
                        df.createOrReplaceTempView("prewarninglogs");
                        //
                        broadcastList = BroadcastUtilsJava.getInstance().getOrUpdateBroadcastList(spark, broadcastList);
                        List<String> keywords = broadcastList.value();
                        String alertSql = "";
                        String statSql = "";
                        if (!keywords.isEmpty()) {
                            for (String keyword : keywords) {
                                alertSql += " logInfo like '%" + keyword + "%' or";
                            }
                            alertSql = alertSql.substring(0,alertSql.length()-2);
                            statSql = "select hostname,servicename,logType,count(1) from prewarninglogs group by hostname,servicename,logType" +
                                    " union all " +
                                    " select t.hostname,t.servicename,t.logType,count(1) " +
                                    " from (select hostname,servicename,'alert' logType from prewarninglogs where " + alertSql +
                                    ") t group by t.hostname,t.servicename,t.logType";

                        } else {
                            statSql = "select hostname,servicename,logType,count(1) from prewarninglogs group by hostname,servicename,logType";
                        }

                        Dataset<Row> statDs = spark.sql(statSql);
                        statDs.show();

                        statDs.foreachPartition(new ForeachPartitionFunction<Row>() {
                            @Override
                            public void call(Iterator<Row> t) throws Exception {
                                String serverURL = "http://"+InfluxDBUtils.getInfluxIP()+":"+InfluxDBUtils.getInfluxPORT(true);
                                String username = "admin";
                                String password = "admin";
                                InfluxDB influxDB  = InfluxDBFactory.connect(serverURL, username, password);
                                String retentionPolicy = InfluxDBUtils.defaultRetentionPolicy(influxDB.version());


                                BatchPoints batchPoints = BatchPoints.database("ruozedata").retentionPolicy(retentionPolicy).build();

                                while (t.hasNext()){
                                    Row row = t.next();
                                    String hostServiceType = String.format("%s_%s_%s",row.getString(0),row.getString(1),row.getString(2));
                                    Long cnts = row.getLong(3);
                                    Point point = Point
                                            .measurement("prewarning")
                                            .tag("host_service_logType", hostServiceType)
                                            .addField("count", cnts)
                                            .build();
                                    batchPoints.point(point);

                                }
                                influxDB.write(batchPoints);
                                influxDB.close();
                            }
                        });


                        /*List<Row> rows = statDs.collectAsList();

                        String value = "";

                        for (Row row : rows) {
                            String host_service_type = row.getString(0) + "_" + row.getString(1) + "_" + row.getString(2);
                            String cnt = row.getLong(3)+"";
                            value += String.format("prewarning,host_service_logType=%s count=%s\n",host_service_type,cnt);
                        }

                        if (value.length() > 0) {
                            //去掉最后一个换行符
                            value = value.substring(0, value.length());
                            System.out.println(value+"========写出到influxDB================");
                            influxDB.write("ruozedata", retentionPolicy, InfluxDB.ConsistencyLevel.ONE, value);
                        }*/


                    }
                }
            });

            jssc.start();
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
