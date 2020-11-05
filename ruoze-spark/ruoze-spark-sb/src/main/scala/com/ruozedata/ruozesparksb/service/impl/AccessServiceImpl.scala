package com.ruozedata.ruozesparksb.service.impl

import com.ruozedata.ruozesparksb.service.AccessService
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AccessServiceImpl @Autowired()(val spark: SparkSession) extends AccessService with Logging {


  override def processAccessLog(date: String): Unit = {
    new Thread(new Runnable {
      override def run(): Unit = {
        doProcessAccessLog(date)
      }
    }).start
  }

  def doProcessAccessLog(date: String): Unit = {

    val conf: SparkConf = spark.sparkContext.getConf
    val filePath = conf.get("spark.dw.raw.path", "/ruozedata/dw/raw/hbase")
    val zk = conf.get("spark.hbase.zookeeper.quorum", "hadoop:2181")
    val hbaseTable = conf.get("spark.hbase.table", "ruozedata:access_log")
    val hostname = conf.get("spark.etl.hostname", "hadoop")
//    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", s"hdfs://${hostname}:9000")

    val mysqlTable = conf.get("spark.stat.mysql.table", "dwd_access_province_traffic")

    val executionTime: String = conf.get("spark.execute.time", "20190101")
    val input = s"${filePath}/${date}"

    val tableSchema = "(ip string,proxyIp string,responseTime string,referer string,method string,url string,httpCode string,requestSize string,responseSize string,cache string,uaHead string,fileType string,province string,city string,isp string,http string,domain string,path string,params string,year string,month string,day string,hour string,minute string,second string,time string)"
    val hbaseFormat = "com.ruozedata.ruozesparksb.etl.hbase"
    val rawFormat = "com.ruozedata.ruozesparksb.etl.access"

    //解析raw文件
    logError(s"====================开始处理${date}的数据")
    val rawDf = spark.read.format(rawFormat).load(input)
    rawDf.show(10, false)
    logError("=============开始写入到HBase================")
    //写入到HBase
    rawDf.write
      .option("hbase.zookeeper.quorum", zk)
      .option("hbase.table", hbaseTable)
      .option("spark.table.schema", tableSchema)
      .format(hbaseFormat).save()

    logError("=============开始从HBase读取数据================")
    //读取HBase数据
    val hbaseDF = spark.read
      .option("hbase.zookeeper.quorum", zk)
      .option("hbase.table", hbaseTable)
      .option("spark.table.schema", tableSchema)
      .format(hbaseFormat).load()
    hbaseDF.show(10, false)
    //注册成临时表
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
  }
}
