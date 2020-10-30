package com.ruozedata.recommend.app

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions

object MemberApp extends Logging {


  def main(args: Array[String]): Unit = {

    System.setProperty("HADOOP_USER_NAME", "hadoop")

    val conf = new SparkConf()
    val spark: SparkSession = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate()
    import spark.implicits._


    val config: Config = ConfigFactory.load()
    val database: String = config.getString("database")
    val memberTable: String = config.getString("memberTable")
    val url: String = config.getString("url")
    val username: String = config.getString("username")
    val password: String = config.getString("password")

    val member: DataFrame = spark.read.format("jdbc")
      .option(JDBCOptions.JDBC_URL, url)
      .option(JDBCOptions.JDBC_TABLE_NAME, s"${database}.${memberTable}")
      .option("user", username)
      .option("password", password)
      .load()


    member.createOrReplaceTempView("member")

    memberSexStat
//    memberChannelStat
//    memberWXStat

    def memberWXStat: Unit = {
      logError("订阅统计........")
      val sql =
        """
          |select
          |count(if(wx != '' and wx is not null,id,null)) subs,
          |count(if(wx == '' or wx is null ,id,null)) un_subs
          |from
          |member
          |
          |""".stripMargin

      val result: DataFrame = spark.sql(sql)

      result
        .write
        .mode(SaveMode.Overwrite)
        .format("hive")
        .option("fileFormat", "orc")
        .option("compression", "zlib")
        .saveAsTable(config.getString("wx.app.output"))

      logError("订阅统计写入Hive。。。。。。。")

      spark.table(config.getString("wx.app.output"))
        .write.mode(SaveMode.Overwrite)
        .option(JDBCOptions.JDBC_URL, config.getString("target.url"))
        .option(JDBCOptions.JDBC_TABLE_NAME, config.getString("mysql.wx.table"))
        .option("user", username)
        .option("password", password)
        .format("jdbc")
        .save()

      logError("订阅统计插入MySQL..........")
    }

    def memberSexStat: Unit = {
      logError("性别统计........")
      val sql =
        """
          |select sex,count(1) cnts from member group by sex
          |""".stripMargin

      val result: DataFrame = spark.sql(sql)

      result
        .write
        .mode(SaveMode.Overwrite)
        .format("hive")
        .option("fileFormat", "orc")
        .option("compression", "zlib")
        .saveAsTable(config.getString("sex.app.output"))

      logError("性别统计写入Hive。。。。。。。")

      spark.table(config.getString("sex.app.output"))
        .write.mode(SaveMode.Overwrite)
        .option(JDBCOptions.JDBC_URL, config.getString("target.url"))
        .option(JDBCOptions.JDBC_TABLE_NAME, config.getString("mysql.sex.table"))
        .option("user", username)
        .option("password", password)
        .format("jdbc")
        .save()

      logError("性别统计插入MySQL..........")
    }

    def memberChannelStat: Unit = {
      logError("渠道统计........")
      val sql =
        """
          |select channel,count(1) cnt from member group by channel
          |""".stripMargin

      val result: DataFrame = spark.sql(sql)

      result
        .write
        .mode(SaveMode.Overwrite)
        .format("hive")
        .option("fileFormat", "orc")
        .option("compression", "zlib")
        .saveAsTable(config.getString("channel.app.output"))

      logError("渠道统计写入Hive。。。。。。。")

      spark.table(config.getString("channel.app.output"))
        .write.mode(SaveMode.Overwrite)
        .option(JDBCOptions.JDBC_URL, config.getString("target.url"))
        .option(JDBCOptions.JDBC_TABLE_NAME, config.getString("mysql.channel.table"))
        .option("user", username)
        .option("password", password)
        .format("jdbc")
        .save()

      logError("渠道统计插入MySQL..........")
    }



    spark.stop()
  }

}
