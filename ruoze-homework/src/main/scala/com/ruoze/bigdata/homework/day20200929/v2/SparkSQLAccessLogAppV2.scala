package com.ruoze.bigdata.homework.day20200929.v2

import com.ruoze.bigdata.homework.day20200929.caseClass.CustomConf
import com.ruoze.bigdata.homework.day20200929.utils.FileUtils
import com.ruoze.bigdata.homework.day20200929.v2.implicits.RichConfiguration
import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.internal.SQLConf
import java.time.format.DateTimeFormatter

object SparkSQLAccessLogAppV2 {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "hadoop")

    val rawCommitProtocal = "com.ruoze.bigdata.homework.day20200929.v2.commitProtocol.RawHadoopMapReduceCommitProtocol"

    val conf = new SparkConf()
      .set(SQLConf.FILE_COMMIT_PROTOCOL_CLASS.key, rawCommitProtocal)
      .setAppName(this.getClass.getSimpleName)
      .setMaster("local")

    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses(Array(classOf[Configuration],classOf[DateTimeFormatter]))

    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()
    import spark.implicits._

//    implicit def hadoopConf2RichHadoopConf(conf:Configuration) = new RichConfiguration(conf)

    val hadoopConfiguration: Configuration = spark.sparkContext.hadoopConfiguration

    hadoopConfiguration.set("fs.defaultFS", "hdfs://hadoop:9000")

    //读取相应配置
    val rawPath: String = conf.get("spark.raw.path", "/ruozedata/data/access.txt")
    val etlPath: String = conf.get("spark.etl.path", "/ruozedata/etl/log")
    val uploadPath: String = conf.get("spark.upload.path", "/ruozedata/log")
    val confName: String = conf.get("spark.etl.conf.name", "my_custom_conf")
    val jobConfKeyPrefix = confName + "|"

    //删除目录
    FileUtils.delete(hadoopConfiguration, etlPath)
    FileUtils.delete(hadoopConfiguration, uploadPath)

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
      val confName: String = row.getAs[String]("conf_name")
      val jobConfName = jobConfKeyPrefix + domain
      val customConf: CustomConf = CustomConf(confName, domain
        , row.getAs[String]("compress_codec")
        , row.getAs[String]("file_name_prefix")
        , row.getAs[String]("file_name_time_format")
        , row.getAs[String]("file_name_suffix")
        , row.getAs[String]("log_content_fields")
        , row.getAs[String]("fields_delimiter"))
      (jobConfName,customConf.toString)
    }).rdd.collectAsMap()

    for (elem <- confMap) {
      if(elem._1.startsWith(jobConfKeyPrefix)){
        hadoopConfiguration.set(elem._1,elem._2)
      }
    }

    //把作业配置的前缀名分别放入hadoop conf 和 confMap中
    hadoopConfiguration.set("job.conf.prefix", jobConfKeyPrefix)
    confMap += ("job.conf.prefix" -> jobConfKeyPrefix)
    confMap += ("format" -> "rawCustomFormat")
    val customRawFormat = "com.ruoze.bigdata.homework.day20200929.v2.customDataSource.raw"

    val rawDF: DataFrame = spark.read.format(customRawFormat).load(rawPath)

    //全日志、全字段、无压缩写出
    rawDF.write.mode(SaveMode.Overwrite).option("sep", "\t").format(customRawFormat).save(etlPath)

    confMap = confMap.updated("format", "uploadCustomFormat")

    val customUploadFormat = "com.ruoze.bigdata.homework.day20200929.v2.customDataSource.upload"
    val etlDF: DataFrame = spark.read.format(customUploadFormat).load(etlPath)

    etlDF.printSchema()
    etlDF.show(false)

    val uploadCommitProtocal = "com.ruoze.bigdata.homework.day20200929.v2.commitProtocol.UploadHadoopMapReduceCommitProtocol"

    spark.conf.unset(SQLConf.FILE_COMMIT_PROTOCOL_CLASS.key)
    spark.conf.set(SQLConf.FILE_COMMIT_PROTOCOL_CLASS.key, uploadCommitProtocal)

    etlDF.write.format(customUploadFormat).options(confMap).mode(SaveMode.Append).save(uploadPath)


    spark.stop()
  }

}
