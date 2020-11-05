package com.ruozedata.ruozesparksb;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RuozeSparkSbApplication {

	public static void main(String[] args) {
		SpringApplication.run(RuozeSparkSbApplication.class, args);
	}


	@Bean
	public SparkConf sparkConf(){
		return new SparkConf();
	}

	@Bean
	public SparkSession sparkSession() {
		System.setProperty("HADOOP_USER_NAME", "hadoop");
		SparkConf conf = sparkConf();
		SparkSession spark = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate();
		return spark;
	}

}
