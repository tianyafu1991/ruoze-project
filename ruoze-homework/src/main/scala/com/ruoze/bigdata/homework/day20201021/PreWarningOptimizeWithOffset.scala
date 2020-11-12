package com.ruoze.bigdata.homework.day20201021

import com.ruoze.bigdata.homework.day20201018.CDHLog
import com.ruoze.bigdata.homework.day20201021.utils.{BroadcastUtils, InfluxDBUtils}
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, TaskContext}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.internal.Logging
import org.apache.spark.sql.execution.streaming.CommitMetadata.format
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, HasOffsetRanges, KafkaUtils, LocationStrategies, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.influxdb.InfluxDB.ConsistencyLevel
import org.influxdb.dto.{BatchPoints, Point}
import org.influxdb.{InfluxDB, InfluxDBFactory}
import org.json4s.DefaultFormats
import org.json4s.jackson.Json

/**
 * 优化collectAsList()
 * 并添加offset提交，offset管理到MySQL
 * scala版本
 */
object PreWarningOptimizeWithOffset extends Logging {

  var updatedBroadcast: Broadcast[List[String]] = _

  def main(args: Array[String]): Unit = {
    prewarning
  }

  def prewarning(): Unit = {

    val conf = new SparkConf()
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses(Array(classOf[ConsumerRecord[String, String]]))
    val ssc = new StreamingContext(conf, Seconds(5))

    //定义Kafka相关参数
    val kafkaParams = Map[String, Object](
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG -> "hadoop01:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "ruozedata",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )
    val topics = Array("PREWARNING")

    //对接Kafka
    val streams: DStream[String] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    ).map(_.value())

    //获取到这个stream中的offset
    var offsetRanges: Array[OffsetRange] = null
    val lines: DStream[String] = streams.transform(rdd => {
      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      offsetRanges.foreach(x => {
        logError(s"${x.topic}, ${x.partition}, ${x.fromOffset}, ${x.untilOffset}")
      })
      rdd
    })

    //经过过滤后将json转换为对象后，开窗
    val windowDStream: DStream[CDHLog] = lines
      .filter(json => {
        json.contains("INFO") || json.contains("WARN") || json.contains("ERROR") || json.contains("DEBUG") || json.contains("FATAL")
      })
      .map(json => {
        val value = Json(DefaultFormats).parse(json)
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

        var alertSql = ""
        //广播变量
        updatedBroadcast = BroadcastUtils.updateBroadcastValue(spark, updatedBroadcast)
        val alertWords: List[String] = updatedBroadcast.value
        var statSql = ""
        if (alertWords.size > 0) {
          alertWords
            .foreach(x => {
              logError(s"=======预警关键词${x}=========")
              alertSql += s" logInfo like '%${x}%' or"
            })
          alertSql = alertSql.substring(0, alertSql.length - 2)
          statSql =
            s"""
               |SELECT hostName,serviceName,logType,COUNT(logType)
               |from prewarninglogs
               |group by
               |hostName,serviceName,logType
               |union all
               |select
               |t.hostname,t.servicename,t.logType,count(t.logType)
               |from (select hostname,servicename,'alert' logType from prewarninglogs where ${alertSql}) t
               |group by
               |t.hostname,t.servicename,t.logType
               |""".stripMargin
        } else {
          statSql =
            """
              |SELECT hostName,serviceName,logType,COUNT(logType)
              |FROM prewarninglogs
              |GROUP BY
              |hostName,serviceName,logType
              |""".stripMargin
        }

        val statDF: DataFrame = spark.sql(statSql)
        statDF.rdd.foreachPartition(partition => {
          //获取到这个partition的offsetRange
          val offsetRange: OffsetRange = offsetRanges(TaskContext.get.partitionId)

          val influxDB: (InfluxDB, String) = generateInfluxDB
          val batchPoints: BatchPoints = BatchPoints
            .database("ruozedata")
            .retentionPolicy(influxDB._2)
//            .consistency(ConsistencyLevel.ALL) 一致性级别
            .build

          partition.foreach(row => {
            val host_service_type = s"${row.getString(0)}_${row.getString(1)}_${row.getString(2)}"
            val cnt = row.getLong(3)
            val point = Point
              .measurement("prewarning")
              .tag("host_service_logType", host_service_type)
              .addField("count", cnt)
              .build()
            batchPoints.point(point)
          })
          influxDB._1.write(batchPoints)
          influxDB._1.close
        })


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
