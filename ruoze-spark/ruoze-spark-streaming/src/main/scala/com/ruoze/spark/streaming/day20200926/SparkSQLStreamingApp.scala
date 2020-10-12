package com.ruoze.spark.streaming.day20200926

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkSQLStreamingApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(conf,Seconds(5))


    ssc.socketTextStream("hadoop01",9527)
    .flatMap(_.split(","))
      .foreachRDD(rdd => {
        if(!rdd.isEmpty()){
          val spark = SparkSession.builder().config(rdd.sparkContext.getConf).getOrCreate()
          import spark.implicits._

          rdd.toDF("word").createOrReplaceTempView("words")
          spark.sql(
            """
              |select
              |word,count(*) cnt
              |from words
              |group by word
              |
              |""".stripMargin).show()
        }
      })


    ssc.start()
    ssc.awaitTermination()
  }

}
