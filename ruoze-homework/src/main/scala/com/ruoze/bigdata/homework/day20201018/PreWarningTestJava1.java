package com.ruoze.bigdata.homework.day20201018;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
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
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class PreWarningTestJava1 {

    public static void main(String[] args) {
        try {
            SparkConf conf = new SparkConf();
            conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
            conf.registerKryoClasses(new Class[]{ConsumerRecord.class});
            SparkSession spark = SparkSession.builder().config(conf).getOrCreate();
            JavaStreamingContext jssc = new JavaStreamingContext(new JavaSparkContext(spark.sparkContext()), new Duration(5000));

            Map<String, Object> kafkaParams = new HashMap<>();
            kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"hadoop01:9092");
            kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
            kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
            kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG,"ruozedata_java");
            kafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");
            kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
            String[] topics = new String[]{"PREWARNING"};
            JavaInputDStream<ConsumerRecord<String, String>> lines = KafkaUtils.createDirectStream(jssc,
                    LocationStrategies.PreferConsistent(),
                    ConsumerStrategies.Subscribe(Arrays.asList(topics), kafkaParams));

            /*lines.filter(new Function<ConsumerRecord<String, String>, Boolean>() {
                @Override
                public Boolean call(ConsumerRecord<String, String> record) throws Exception {
                    String value = record.value();
                    return (value.contains("INFO") || value.contains("ERROR") || value.contains("WARNING") || value.contains("DEBUG") || value.contains("FATAL")
                            )&& (value.contains("hostname") && value.contains("servicename"));
                }
            }).map(new Function<ConsumerRecord<String, String>, String>() {
                @Override
                public String call(ConsumerRecord<String, String> v1) throws Exception {
                    return v1.value();
                }
            })
                    .print();*/


            JavaDStream<CDHLogJava> cdhLogDStream = lines.filter(new Function<ConsumerRecord<String, String>, Boolean>() {
                @Override
                public Boolean call(ConsumerRecord<String, String> record) throws Exception {
                    String value = record.value();
                    return (value.contains("INFO") || value.contains("ERROR") || value.contains("WARNING") || value.contains("DEBUG") || value.contains("FATAL")
                    )&& (value.contains("hostname") && value.contains("servicename"));
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
                    if(cdhLogJavaJavaRDD.isEmpty()){
                        System.out.println("========没有数据=================");
                    }else {
                        Dataset<Row> df = spark.createDataFrame(cdhLogJavaJavaRDD, CDHLogJava.class);
                        df.show();
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

