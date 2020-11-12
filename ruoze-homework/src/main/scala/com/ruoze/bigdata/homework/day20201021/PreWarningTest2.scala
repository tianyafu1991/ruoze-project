package com.ruoze.bigdata.homework.day20201021

import java.util

import com.ruoze.bigdata.homework.day20201018.CDHLog
import com.ruoze.bigdata.homework.day20201021.utils.InfluxDBUtils
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.influxdb.{InfluxDB, InfluxDBFactory}
import org.json4s.DefaultFormats
import org.json4s.jackson.Json
import org.apache.spark.sql.execution.streaming.CommitMetadata.format
import org.influxdb.dto.Point

/**
 * 测试写入influxDB
 * scala版本
 */
object PreWarningTest2 extends Logging {

  def main(args: Array[String]): Unit = {
    prewarning
  }

  def prewarning(): Unit = {
    val influxDB: (InfluxDB, String) = generateInfluxDB
    val conf = new SparkConf()
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses(Array(classOf[ConsumerRecord[String, String]]))
    val ssc = new StreamingContext(conf, Seconds(5))
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
        val cdhLogDS: Dataset[CDHLog] = rdd.toDS()
        cdhLogDS.createOrReplaceTempView("prewarninglogs")
        val sqlStr =
          """
            |SELECT hostName,serviceName,logType,COUNT(logType)
            |FROM prewarninglogs
            |GROUP BY
            |hostName,serviceName,logType
            |""".stripMargin

        val rows: util.List[Row] = spark.sql(sqlStr).collectAsList()
        var value = ""

        for (i <- 0 until rows.size()) {
          val row: Row = rows.get(i)
          val host_service_type = s"${row.getString(0)}_${row.getString(1)}_${row.getString(2)}"
          val cnt = row.getLong(3).toString
          value += s"prewarning,host_service_logType=${host_service_type} count=${cnt}\n"
        }

        if(value.length > 0){
          //去掉最后一个换行符
          value = value.substring(0,value.length)
          logError(s"========================${value}")
          influxDB._1.write("ruozedata",influxDB._2, InfluxDB.ConsistencyLevel.ONE,value)
        }


      } else {
        logError("没有数据.............")
      }
    })


    ssc.start()
    ssc.awaitTermination()
  }


  /**
   * https://github.com/influxdata/influxdb-java
   *
   * @return 返回一个influxDB的连接和一个retentionPolicy
   */
  def generateInfluxDB(): (InfluxDB, String) = {
    val serverURL = s"http://${InfluxDBUtils.getInfluxIP}:${InfluxDBUtils.getInfluxPORT(true)}"
    val username = "admin"
    val password = "admin"
    val influxDB: InfluxDB = InfluxDBFactory.connect(serverURL, username, password)
    val retentionPolicy: String = InfluxDBUtils.defaultRetentionPolicy(influxDB.version())
    (influxDB, retentionPolicy)
  }

}
