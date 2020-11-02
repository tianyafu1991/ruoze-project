package com.ruoze.bigdata.homework.day20201014.app

import org.apache.hadoop.hbase.client.Put
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkHBaseRowKeyApp extends Logging {


  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses(Array(classOf[Put]))
    val spark = SparkSession.builder().config(conf).getOrCreate()

    System.setProperty("HADOOP_USER_NAME", "hadoop")
    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", s"hdfs://hadoop:9000")

    val filePath = conf.get("spark.dw.raw.path", "/ruozedata/dw/raw/hbase")
    val executionTime: String = conf.get("spark.execute.time", "20190101")
    val input = s"${filePath}/${executionTime}"

    //hbase相关配置
    val zk = conf.get("spark.hbase.zookeeper.quorum", "hadoop:2181")
    val hbaseOutTable = conf.get("spark.hbase.out.table", "ruozedata:access_log")
    val rowKeyFiledName: String = conf.get("hbase.table.rowkey.filed.name", "minute")
    val hbaseInTable = conf.get("spark.hbase.in.table", hbaseOutTable)

    //定义HBase table schema
    val tableSchema = "(rowKey string,ip string,proxyIp string,responseTime int,referer string,method string,url string,httpCode string,requestSize string,responseSize string,cache string,uaHead string,fileType string,province string,city string,isp string,http string,domain string,path string,params string,year string,month string,day string,hour string,minute string,second string,time string)"

    //读取raw数据
    val rawFormat = "com.ruoze.bigdata.homework.day20201014.raw"
    val rawDF: DataFrame = spark.read.format(rawFormat).load(input)
    rawDF.show(10, false)
    logError("读取raw数据成功")

    //落地到HBase
    val hbaseFormat = "com.ruoze.bigdata.homework.day20201014.hbase"
    rawDF
      .write
      .option("hbase.zookeeper.quorum", zk)
      .option("hbase.out.table", hbaseOutTable)
      .option("hbase.table.rowkey.filed.name", rowKeyFiledName)
      .format(hbaseFormat)
      .save()
    logError("写入HBase成功......")

    //读取HBase的数据
    val hbaseDF: DataFrame = spark
      .read
      .option("hbase.zookeeper.quorum", zk)
      .option("hbase.in.table", hbaseInTable)
      .option("hbase.table.rowkey.filed.name", rowKeyFiledName)
//      .option("spark.table.schema", tableSchema)
      .option("hbase.table.column.family","f")
      .format(hbaseFormat)
      .load()

    hbaseDF.show(false)


    spark.stop()
  }

}
