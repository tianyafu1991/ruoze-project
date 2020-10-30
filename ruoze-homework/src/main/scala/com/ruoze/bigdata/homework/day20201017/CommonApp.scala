package com.ruoze.bigdata.homework.day20201017

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions

object CommonApp extends Logging {

  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "hadoop")
    val conf = new SparkConf()
    val spark: SparkSession = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate()
    import spark.implicits._

    //source相关配置
    val sourceFormat = conf.get("spark.source.format", "jdbc")
    val sourceUrl = conf.get("spark.source.url", "jdbc:mysql://hadoop:3306/ruozedata_platform?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8")
    val sourceTable = conf.get("spark.source.table", "member")
    val sourceUser = conf.get("spark.source.user", "root")
    val sourcePassword = conf.get("spark.source.password", "root")
    val sourceDriver = conf.get("spark.source.driver", "com.mysql.jdbc.Driver")
    val sourceView = conf.get("spark.source.view.name", "source")

    //hive相关配置
    val hiveFormat = "hive"
    val hiveFileFormat = conf.get("spark.hive.file.format", "orc")
    val hiveCompression = conf.get("spark.hive.compression", "zlib")
    val hiveDBName = conf.get("spark.hive.database.name", "dw")
    val hiveTableName = conf.get("spark.hive.table.name", "app_sex_stat")
    val hiveSaveMode = conf.get("spark.hive.save.mode", "overwrite")

    //target相关配置
    val targetFormat = conf.get("spark.target.format", "jdbc")
    val targetUrl = conf.get("spark.target.url", "jdbc:mysql://hadoop:3306/ruozedata_recommend?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8")
    val targetTable = conf.get("spark.target.table", "sex_stat")
    val targetUser = conf.get("spark.target.user", "root")
    val targetPassword = conf.get("spark.target.password", "root")
    val targetDriver = conf.get("spark.target.driver", "com.mysql.jdbc.Driver")
    val targetSaveMode = conf.get("spark.target.save.mode", "overwrite")

    //读取源数据
    val source: DataFrame = readSource(spark, sourceDriver, sourceUrl, sourceTable, sourceUser, sourcePassword, sourceFormat)
    source.show(false)
    source.createOrReplaceTempView(sourceView)

    //统计数据
    val statResult: DataFrame = ProcessData(spark, sourceView)
    statResult.show(false)
    //统计结果写出到Hive
    doStatResultDump(statResult, hiveSaveMode, hiveFileFormat, hiveCompression, hiveFormat, hiveDBName, hiveTableName)
    //读取Hive中的统计结果
    val statResultRead: DataFrame = doStatResultRead(spark, hiveDBName, hiveTableName)
    statResultRead.show(false)
    //写出到MySQL中
    doStatResultWrite2Target(statResultRead, targetSaveMode, targetDriver, targetUrl, targetTable, targetUser, targetPassword, targetFormat)

    logError("任务完成........")

    spark.stop()

  }

  def readSource(spark: SparkSession, sourceDriver: String, sourceUrl: String, sourceTable: String, sourceUser: String, sourcePassword: String, sourceFormat: String): DataFrame = {
    logError("开始读取源数据..........")
    val source: DataFrame = spark
      .read
      .option(JDBCOptions.JDBC_DRIVER_CLASS, sourceDriver)
      .option(JDBCOptions.JDBC_URL, sourceUrl)
      .option(JDBCOptions.JDBC_TABLE_NAME, sourceTable)
      .option("user", sourceUser)
      .option("password", sourcePassword)
      .format(sourceFormat)
      .load()
    source
  }

  def ProcessData(spark: SparkSession, sourceView: String): DataFrame = {
    logError("开始数据处理........")
    val defaultSql =
      s"""
         |select sex,count(1) cnts from ${sourceView} group by sex
         |""".stripMargin

    val statSql = spark.sparkContext.getConf.get("spark.stat.sql", defaultSql)
    val result: DataFrame = spark.sql(statSql)
    result
  }

  def doStatResultDump(statResult: DataFrame, hiveSaveMode: String, hiveFileFormat: String, hiveCompression: String, hiveFormat: String, hiveDBName: String, hiveTableName: String): Unit = {
    logError("开始写入Hive........")
    val mode: SaveMode = getSaveMode(hiveSaveMode)
    statResult
      .write
      .mode(mode)
      .option("fileFormat", hiveFileFormat)
      .option("compression", hiveCompression)
      .format(hiveFormat)
      .saveAsTable(s"${hiveDBName}.${hiveTableName}")
  }

  def getSaveMode(saveMode: String): SaveMode = {
    saveMode match {
      case "overwrite" => SaveMode.Overwrite
      case "append" => SaveMode.Append
      case "errorIfExists" => SaveMode.ErrorIfExists
      case "ignore" => SaveMode.Ignore
      case _ => SaveMode.Overwrite
    }
  }

  def doStatResultRead(spark: SparkSession, hiveDBName: String, hiveTableName: String): DataFrame = {
    logError("开始读取Hive统计结果........")
    val hiveDF: DataFrame = spark.table(s"${hiveDBName}.${hiveTableName}")
    hiveDF
  }

  def doStatResultWrite2Target(statResultRead: DataFrame, targetSaveMode: String, targetDriver: String, targetUrl: String, targetTable: String, targetUser: String, targetPassword: String, targetFormat: String): Unit = {
    logError(s"开始写入MySQL........${targetSaveMode}|${targetDriver}|${targetUrl}|${targetTable}|${targetUser}|${targetPassword}|${targetFormat}")
    statResultRead
      .write
      .mode(getSaveMode(targetSaveMode))
      .option(JDBCOptions.JDBC_DRIVER_CLASS, targetDriver)
      .option(JDBCOptions.JDBC_URL, targetUrl)
      .option(JDBCOptions.JDBC_TABLE_NAME, targetTable)
      .option("user", targetUser)
      .option("password", targetPassword)
      .format(targetFormat)
      .save()
  }


}
