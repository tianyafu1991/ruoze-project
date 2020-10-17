package com.ruoze.bigdata.homework.day20200929

import com.ruoze.bigdata.homework.day20200929.caseClass.CustomConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object MyTest {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().master("local").appName(this.getClass.getSimpleName).getOrCreate()

    val confName = "my_custom_conf"
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
    val confDF: DataFrame = spark.read.format("jdbc")
      .option(JDBCOptions.JDBC_URL, "jdbc:mysql://hadoop:3306/ruozedata?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8")
      .option(JDBCOptions.JDBC_DRIVER_CLASS, "com.mysql.jdbc.Driver")
      .option(JDBCOptions.JDBC_TABLE_NAME, confSql)
      .option("user", "root")
      .option("password", "root")
      .load()
    import spark.implicits._

    val customConfDF: Dataset[(String, CustomConf)] = confDF.map(row => {
      val domain: String = row.getAs[String]("domain")
      (domain,
        CustomConf(row.getAs[String]("conf_name")
          , domain
          , row.getAs[String]("compress_codec")
          , row.getAs[String]("file_name_prefix")
          , row.getAs[String]("file_name_time_format")
          , row.getAs[String]("file_name_suffix")
          , row.getAs[String]("log_content_fields")
          , row.getAs[String]("fields_delimiter")))
    })
    val confMap: collection.Map[String, CustomConf] = customConfDF.rdd.collectAsMap()


    customConfDF.printSchema()
    customConfDF.show(false)


    spark.stop()
  }

}
