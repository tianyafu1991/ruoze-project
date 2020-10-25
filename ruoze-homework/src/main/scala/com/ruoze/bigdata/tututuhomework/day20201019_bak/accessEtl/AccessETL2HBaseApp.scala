package com.ruoze.bigdata.tututuhomework.day20201019_bak.accessEtl

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object AccessETL2HBaseApp extends Logging {

  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "hadoop")
    val conf = new SparkConf()
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()

    val hostname = "hadoop01"

    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", s"hdfs://${hostname}:9000")

    val zkAddress = conf.get("spark.hbase.zookeeper.quorum",s"${hostname}:2181")

    val readFormat = "com.ruoze.bigdata.tututuhomework.day20201019.accessEtl.raw"
    val hbaseFormat = "com.ruoze.bigdata.tututuhomework.day20201019.accessEtl.hbase"
    val tableSchema = "(ip string,proxyIp string,responseTime string,referer string,method string,url string,httpCode string,requestSize string,responseSize string,cache string,uaHead string,fileType string,province string,city string,isp string,http string,domain string,path string,params string,year string,month string,day string,hour string,minute string,second string,time string)"
    val executionTime: String = conf.get("spark.execute.time", "20190101")
    val rawPathConf: String = conf.get("spark.dw.raw.path", "/ruozedata/dw/raw/hbase")
    val input = s"${rawPathConf}/${executionTime}"
//    val input = "data"
    val accessDF: DataFrame = spark.read.format(readFormat).load(input)
      logError("从文本中查询出来")
      accessDF.show()
    accessDF
      .write
      .format(hbaseFormat)
      .option("hbase.table","ruozedata:access_log")
      .option("hbase.zookeeper.quorum","hadoop01:2181")
      .option("spark.table.schema", tableSchema)
      .save()



    val hbaseDF: DataFrame = spark.read
      .format(hbaseFormat)
      .option("hbase.table", "ruozedata:access_log")
      .option("hbase.zookeeper.quorum", "hadoop01:2181")
      .option("spark.table.schema", tableSchema)
      .load()

    logError("这个是从HBase中查出来的")
    hbaseDF.show(10,false)

    hbaseDF.createOrReplaceTempView("access")

    val statSql =
      """
        |select province,sum(responseSize) traffics,count(1) cnt from access group by province
        |""".stripMargin

    val statDF: DataFrame = spark.sql(statSql)
      statDF.show(false)

    statDF.write
        .option("url",s"jdbc:mysql://${hostname}:3306/ruozedata?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8")
      .option("dbtable","dwd_access_province_traffic")
      .option("driver","com.mysql.jdbc.Driver")
      .option("user","root")
      .option("password","root")
      .mode(SaveMode.Append)
      .format("jdbc")
      .save()

    logError("完成了")


    spark.stop()
  }

}
