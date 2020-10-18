package com.ruoze.bigdata.homework.day20200929.v2.customDataSource.upload

import com.ruoze.bigdata.homework.day20200929.caseClass.CustomConf
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.internal.Logging
import org.apache.spark.sql
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.StructType

import scala.reflect.internal.util.TableDef.Column

class DefaultSource extends RelationProvider with SchemaRelationProvider with CreatableRelationProvider with Logging {

  //RelationProvider要实现的方法
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext, parameters, null)
  }

  //SchemaRelationProvider要实现的方法
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
    val path: Option[String] = parameters.get("path")
    path match {
      case Some(p) => new CustomUploadDataSourceRelation(sqlContext, p, schema)
      case _ => throw new IllegalArgumentException("path is not exists....")
    }
  }


  //CreatableRelationProvider要实现的方法
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
    println("CreatableRelationProvider要实现的方法")
    val path: String = parameters.getOrElse("path", "/ruozedata/log")
    val fsPath = new Path(path)
    val fs: FileSystem = fsPath.getFileSystem(sqlContext.sparkContext.hadoopConfiguration)

    mode match {
      case SaveMode.Append => logError("append模式")
      case SaveMode.Overwrite => fs.delete(fsPath, true)
      case SaveMode.ErrorIfExists => sys.error("Given path: " + path + " already exists!!"); sys.exit(1)
      case SaveMode.Ignore => sys.exit()
    }

    val format: String = parameters.getOrElse("format", "rawCustomFormat")
    format match {
      case "uploadCustomFormat" => saveAsuploadCustomFormat(data, path, mode, parameters)
      case "csv" => saveAsCsvFormat(data, path, mode, parameters)
      case "json" => saveAsJsonFormat(data, path, mode, parameters)
    }
    createRelation(sqlContext, parameters, data.schema)
  }

  def saveAsJsonFormat(data: DataFrame, path: String, mode: SaveMode, parameters: Map[String, String]): Unit = {
    data.write.mode(mode).options(parameters).json(path)
  }


  def saveAsCsvFormat(data: DataFrame, path: String, mode: SaveMode, parameters: Map[String, String]): Unit = {
    data.write.mode(mode).options(parameters).csv(path)
  }

  def saveAsuploadCustomFormat(data: DataFrame, path: String, mode: SaveMode, parameters: Map[String, String]): Unit = {
    import data.sparkSession.implicits._
    println("自定义上传日志写出格式")
    //获取本次job.name和本次job的配置
    val jobConfKeyPrefix: String = parameters.getOrElse("job.conf.prefix", "my_custom_conf|")
    var jobConfList = List[CustomConf]()
    for (elem <- parameters) {
      if (elem._1.startsWith(jobConfKeyPrefix)) {
        logError(f"key is ${elem._1}.........value is ${elem._2}")
        val splits: Array[String] = elem._2.split("\t")
        jobConfList = jobConfList :+ CustomConf(splits(0), splits(1), splits(2), splits(3), splits(4), splits(5), splits(6), splits(7))
      }
    }

    for (elem <- jobConfList) {
      val domain: String = elem.domain
      val logContentFields: String = elem.logContentFields
      val delimiter: String = elem.fieldsDelimiter
      val codec: String = elem.compressCodec
      val colList: List[String] = logContentFields.split(",").toList
      var columns: List[sql.Column] = for (elem <- colList) yield data.col(elem)
      val domainCol: sql.Column = data.col("domain").as("partitionDomain")
      val timeCol: sql.Column = data.col("time").as("partitionTime")
      columns = domainCol :: timeCol :: columns
      logError(s"开始写出${domain}的数据")
      data.filter(row => row.getAs[String]("domain") == domain)
        .select(columns: _*)
        .coalesce(1)
        .write
        .partitionBy("partitionDomain", "partitionTime")
        .options(parameters)
        .option("sep", delimiter)
        .option("compression", codec)
        .mode(mode)
        .format("csv")
        .save(path)
    }
  }


}
