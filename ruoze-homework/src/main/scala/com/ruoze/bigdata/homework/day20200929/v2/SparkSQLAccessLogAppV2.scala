package com.ruoze.bigdata.homework.day20200929.v2

import com.ruoze.bigdata.homework.day20200929.caseClass.CustomConf
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.internal.SQLConf

object SparkSQLAccessLogAppV2 {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "hadoop")

    val rawCommitProtocal = "com.ruoze.bigdata.homework.day20200929.v2.raw.RawHadoopMapReduceCommitProtocol"


    val conf = new SparkConf()
      .set(SQLConf.FILE_COMMIT_PROTOCOL_CLASS.key,rawCommitProtocal)
      .setAppName(this.getClass.getSimpleName)
      .setMaster("local")
    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()
    import spark.implicits._

    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", "hdfs://hadoop:9000")

    //读取相应配置
    val rawPath: String = conf.get("spark.raw.path", "/ruozedata/data/access.txt")
    val etlPath: String = conf.get("spark.etl.path", "/ruozedata/etl/log")
    val confName: String = conf.get("spark.etl.conf.name", "my_custom_conf")
    //读取数据库中的配置信息
    val confSql =
      s"""
         | (SELECT
         |	conf_name,
         |	domain,
         |	compress_codec,
         |	ifnull(file_name_prefix,'') file_name_prefix,
         |	file_name_time_format,
         |	file_name_suffix,
         |	log_content_fields,
         |	fields_delimiter FROM spark_log_etl_conf WHERE
         |	conf_name = '${confName}') as conf_sql_temp
         |""".stripMargin

    val confDF: DataFrame = spark
      .read
      .format("jdbc")
      .option(JDBCOptions.JDBC_URL, "jdbc:mysql://hadoop:3306/ruozedata?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8")
      .option(JDBCOptions.JDBC_TABLE_NAME, confSql)
      .option(JDBCOptions.JDBC_DRIVER_CLASS, "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "root")
      .load()

    var confMap: collection.Map[String, String] = confDF.map(row => {
      val domain: String = row.getAs[String]("domain")
      (domain,
        CustomConf(row.getAs[String]("conf_name")
          , domain
          , row.getAs[String]("compress_codec")
          , row.getAs[String]("file_name_prefix")
          , row.getAs[String]("file_name_time_format")
          , row.getAs[String]("file_name_suffix")
          , row.getAs[String]("log_content_fields")
          , row.getAs[String]("fields_delimiter")).toString)
    }).rdd.collectAsMap()

    confMap += ("format" -> "rawCustomFormat")
    val customFormat = "com.ruoze.bigdata.homework.day20200929.v2.customDataSource"

    val rawDF: DataFrame = spark.read.format(customFormat).load(rawPath)
    /*rawDF.printSchema()
    rawDF.show(false)*/
    //全日志、全字段、无压缩写出
    rawDF.write.mode(SaveMode.Overwrite).option("sep","\t").format(customFormat).save(etlPath)

    confMap += ("format" -> "uploadCustomFormat")
    val etlDF: DataFrame = spark.read.format(customFormat).load(etlPath)

    etlDF.printSchema()
    etlDF.show(false)



    spark.stop()
  }

}
