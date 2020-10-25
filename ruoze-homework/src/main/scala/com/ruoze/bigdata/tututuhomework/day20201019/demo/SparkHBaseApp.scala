package com.ruoze.bigdata.tututuhomework.day20201019.demo

import com.ruoze.bigdata.tututuhomework.day20201019.SparkHBaseETLApp.logError
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.spark.SparkConf
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

object SparkHBaseApp extends Logging{

  def main(args: Array[String]): Unit = {
//    System.setProperty("HADOOP_USER_NAME", "hadoop")

    val conf = new SparkConf().set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses(Array(classOf[ImmutableBytesWritable]))
    val spark = SparkSession.builder().config(conf).getOrCreate()
//    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", "hdfs://hadoop01:9000")

    val hTable = "ruozedata:access_log"
    val configuration = HBaseConfiguration.create()
    configuration.set("hbase.zookeeper.property.clientPort", "2181")
    configuration.set("hbase.zookeeper.quorum", "hadoop01")
    configuration.set(TableOutputFormat.OUTPUT_TABLE,hTable)

    val con = ConnectionFactory.createConnection(configuration)
    val admin = con.getAdmin


    val hostname = conf.get("spark.etl.hostname", "hadoop")
    val zkAddress = conf.get("spark.hbase.zookeeper.quorum", "ruozedata001:2181,ruozedata002:2181,ruozedata003:2181")

    val hbaseTable = conf.get("spark.hbase.table", hTable)

    val mysqlTable = conf.get("spark.stat.mysql.table", "dwd_access_province_traffic")

    val readFormat = "com.ruoze.bigdata.tututuhomework.day20201019.etl.raw"

    val hbaseFormat = "com.ruoze.bigdata.tututuhomework.day20201019.etl.hbase"

    val tableSchema = "(ip string,proxyIp string,responseTime string,referer string,method string,url string,httpCode string,requestSize string,responseSize string,cache string,uaHead string,fileType string,province string,city string,isp string,http string,domain string,path string,params string,year string,month string,day string,hour string,minute string,second string,time string)"

    val executionTime: String = conf.get("spark.execute.time", "20190101")
    val rawPathConf: String = conf.get("spark.dw.raw.path", "/ruozedata/dw/raw/hbase")
    val input = s"${rawPathConf}/${executionTime}"

    val accessDF: DataFrame = spark.read.format(readFormat).load(input)
    logError("从文本中查询出来")
    accessDF.show(10, false)

    val putRDD: RDD[(ImmutableBytesWritable, Put)] = accessDF.rdd.mapPartitions(partition => {
      partition.map(row => {
        val rowKey = generateRowKey(row)
        val put = new Put(Bytes.toBytes(rowKey))
        val province = row.getAs[String]("province")
        val time = row.getAs[String]("time")
        val minute = row.getAs[String]("minute")
        put.addColumn(Bytes.toBytes("f"), Bytes.toBytes("province"), Bytes.toBytes(province))
        put.addColumn(Bytes.toBytes("f"), Bytes.toBytes("time"), Bytes.toBytes(time))
        put.addColumn(Bytes.toBytes("f"), Bytes.toBytes("minute"), Bytes.toBytes(minute))

        (new ImmutableBytesWritable(put.getRow), put)
      })
    })

    putRDD.saveAsNewAPIHadoopFile("/ruozedata/dw/tmp/spark_hbase"
      ,classOf[ImmutableBytesWritable],classOf[Put],classOf[TableOutputFormat[ImmutableBytesWritable]],configuration)

    /**
     * 手动将写缓存的数据flush到HDFS磁盘上
     */
    admin.flush(TableName.valueOf(hTable))

    admin.close()
    con.close()



    spark.stop()
  }


  def generateRowKey(row:Row):String={
    val time = row.getAs[String]("time")
    val minute = row.getAs[String]("minute")

    f"${time}_${minute}"
  }

}
