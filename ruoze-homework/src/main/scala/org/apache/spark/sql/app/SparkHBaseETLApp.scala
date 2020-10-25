package org.apache.spark.sql.app

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object SparkHBaseETLApp extends Logging {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    /*System.setProperty("HADOOP_USER_NAME", "hadoop")
    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", s"hdfs://hadoop01:9000")*/

    val hostname = conf.get("spark.etl.hostname", "ruozedata001")
    val zkAddress = conf.get("spark.hbase.zookeeper.quorum", "ruozedata001:2181,ruozedata002:2181,ruozedata003:2181")
    val hbaseTable = conf.get("spark.hbase.table", "ruozedata:access_log")
    val mysqlTable = conf.get("spark.stat.mysql.table", "dwd_access_province_traffic")
    val readFormat = "org.apache.spark.sql.access"
    val hbaseFormat = "org.apache.spark.sql.hbase"
    val tableSchema = "(ip string,proxyIp string,responseTime string,referer string,method string,url string,httpCode string,requestSize string,responseSize string,cache string,uaHead string,fileType string,province string,city string,isp string,http string,domain string,path string,params string,year string,month string,day string,hour string,minute string,second string,time string)"

    val executionTime: String = conf.get("spark.execute.time", "20190101")
    val rawPathConf: String = conf.get("spark.dw.raw.path", "/ruozedata/dw/raw/hbase")
    val input = s"${rawPathConf}/${executionTime}"

    val accessDF: DataFrame = spark.read.format(readFormat).load(input)
    logError("从文本中查询出来")
    accessDF.show(10, false)

    logError("保存到HBase.............")
    accessDF
      .write
      .format(hbaseFormat)
      .option("hbase.table.name", hbaseTable)
      .option("hbase.zookeeper.quorum", zkAddress)
      .option("spark.table.schema", tableSchema)
      .option("hbase.table.rowkey.field","minute")
      .option("hbase.table.family","f")
      .save()

    /*val hbaseDF: DataFrame = spark.read
      .format(hbaseFormat)
      .option("hbase.table", hbaseTable)
      .option("hbase.zookeeper.quorum", zkAddress)
      .option("spark.table.schema", tableSchema)
      .load()

    logError("这个是从HBase中查出来的")
    hbaseDF.show(10, false)

    hbaseDF.coalesce(1).createOrReplaceTempView("access")

    val statSql =
      """
        |select province,sum(responseSize) traffics,count(1) cnt from access group by province
        |""".stripMargin

    val statDF: DataFrame = spark.sql(statSql)
    logError("统计结果")
    statDF.show(false)

    statDF.coalesce(1).write
      .option("url", s"jdbc:mysql://${hostname}:3306/ruozedata?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8")
      .option("dbtable", mysqlTable)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "root")
      .mode(SaveMode.Append)
      .format("jdbc")
      .save()*/

    logError("完成了")

    spark.stop()
  }

}
