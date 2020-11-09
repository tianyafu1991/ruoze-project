package com.ruoze.bigdata.homework.day20201018

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.execution.streaming.CommitMetadata.format
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.json4s.DefaultFormats
import org.json4s.jackson.Json

object PreWarningTest1 extends Logging{

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses(Array(classOf[ConsumerRecord[String, String]]))
    /*val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._*/
    val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))

    /*val checkpoint = "file:///F:\\study\\ruozedata\\ruoze-project\\chk"
    ssc.checkpoint(checkpoint)*/

    val kafkaParams = Map[String, Object](
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG -> "hadoop01:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "ruozedata",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )
    val topics = Array("PREWARNING")

    val lines = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    val windowDStream: DStream[CDHLog] = lines
      .filter(x => {
        val json = x.value()
        json.contains("INFO") || json.contains("WARN") || json.contains("ERROR") || json.contains("DEBUG") || json.contains("FATAL")
      })
      .map(_.value())
      .filter(x => {
        x.contains("hostname") && x.contains("servicename")
      })
      .map(x => {
        val value = Json(DefaultFormats).parse(x)
        val log: CDHLog = value.extract[CDHLog]
        log
      })
      .filter(_ != null)
      .window(Seconds(5), Seconds(5))

    windowDStream.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        val spark = SparkSession.builder().config(rdd.sparkContext.getConf).getOrCreate()
        import spark.implicits._
        rdd.toDS().show(100,false)
      }else{
        logError("没有数据.............")
      }
    })


    ssc.start()
    ssc.awaitTermination()
  }

}

case class CDHLog(hostname: String, servicename: String, time: String, logtype: String, loginfo: String)
