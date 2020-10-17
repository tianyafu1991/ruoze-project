package com.ruoze.bigdata.homework.day20200929.v2.customDataSource.raw

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}

class DefaultSource extends RelationProvider with SchemaRelationProvider with CreatableRelationProvider {

  //RelationProvider要实现的方法
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext, parameters, null)
  }

  //SchemaRelationProvider要实现的方法
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
    val path: Option[String] = parameters.get("path")
    path match {
      case Some(p) => new CustomRawDataSourceRelation(sqlContext, p, schema)
      case _ => throw new IllegalArgumentException("path is not exists....")
    }
  }

  //CreatableRelationProvider要实现的方法
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
    println("CreatableRelationProvider要实现的方法")
    val path: String = parameters.getOrElse("path", "/ruozedata/etl/log")
    val fsPath = new Path(path)
    val fs: FileSystem = fsPath.getFileSystem(sqlContext.sparkContext.hadoopConfiguration)

    mode match {
      case SaveMode.Append => sys.error("Append mode is not supported by " + this.getClass.getCanonicalName); sys.exit(1)
      case SaveMode.Overwrite => fs.delete(fsPath, true)
      case SaveMode.ErrorIfExists => sys.error("Given path: " + path + " already exists!!"); sys.exit(1)
      case SaveMode.Ignore => sys.exit()
    }

    val format: String = parameters.getOrElse("format", "rawCustomFormat")
    format match {
      case "rawCustomFormat" => saveAsRawCustomFormat(data, path, mode, parameters)
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

  def saveAsRawCustomFormat(data: DataFrame, path: String, mode: SaveMode, parameters: Map[String, String]): Unit = {
    import data.sparkSession.implicits._
    println("自定义解析日志写出格式")

    data.select($"*",$"time".as("partitionTime"),$"domain".as("partitionDomain"))
      .coalesce(1)
      .write
      .partitionBy("partitionTime", "partitionDomain")
      .options(parameters)
      .mode(mode)
      .format("csv")
      .save(path)
  }

  def saveAsuploadCustomFormat(data: DataFrame, path: String, mode: SaveMode, parameters: Map[String, String]): Unit = {
    import data.sparkSession.implicits._
    println("自定义上传日志写出格式")

    data.select($"*",$"time".as("partitionTime"),$"domain".as("partitionDomain"))
      .coalesce(1)
      .write
      .partitionBy("time", "domain")
      .options(parameters)
      .mode(mode)
      .format("csv")
      .save(path)
  }
}
