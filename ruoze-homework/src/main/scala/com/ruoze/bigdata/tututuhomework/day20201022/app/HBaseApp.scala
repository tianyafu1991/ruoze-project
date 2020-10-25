package com.ruoze.bigdata.tututuhomework.day20201022.app

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object HBaseApp extends Logging{

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val spark = SparkSession.builder().config(conf).getOrCreate()

    /*System.setProperty("HADOOP_USER_NAME", "hadoop")
    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", s"hdfs://hadoop01:9000")*/

    val filePath = conf.get("spark.dw.raw.path", "/ruozedata/dw/raw/hbase")
    val zk = conf.get("spark.hbase.zookeeper.quorum","hadoop01:2181")
    val hbaseTable = conf.get("spark.hbase.table", "ruozedata:access_log")
    val hostname = conf.get("spark.etl.hostname", "hadoop")



    val mysqlTable = conf.get("spark.stat.mysql.table", "dwd_access_province_traffic")

    val executionTime: String = conf.get("spark.execute.time", "20190101")
    val input = s"${filePath}/${executionTime}"

    val tableSchema = "(ip string,proxyIp string,responseTime string,referer string,method string,url string,httpCode string,requestSize string,responseSize string,cache string,uaHead string,fileType string,province string,city string,isp string,http string,domain string,path string,params string,year string,month string,day string,hour string,minute string,second string,time string)"
    val hbaseFormat = "com.ruoze.bigdata.tututuhomework.day20201022.hbaseIo"
    val rawFormat = "com.ruoze.bigdata.tututuhomework.day20201022.rawIo"

    //解析raw文件
    val rawDf = spark.read.format(rawFormat).load(input)
//    rawDf.show(10,false)

    //写入到HBase
    rawDf.write
      .option("hbase.zookeeper.quorum",zk)
      .option("hbase.table",hbaseTable)
      .option("spark.table.schema",tableSchema)
      .format(hbaseFormat).save()

    //读取HBase数据
    val hbaseDF = spark.read
      .option("hbase.zookeeper.quorum", zk)
      .option("hbase.table", hbaseTable)
      .option("spark.table.schema", tableSchema)
      .format(hbaseFormat).load()

//    hbaseDF.show(10,false)

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
      .save()


    logError("完成了")


    spark.stop()
  }

}
