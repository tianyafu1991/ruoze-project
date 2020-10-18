package com.ruoze.bigdata.homework.day20200929.v2.customDataSource.upload

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.StructType

class DefaultSource extends RelationProvider with SchemaRelationProvider with CreatableRelationProvider with Logging{

  //RelationProvider要实现的方法
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext,parameters,null)
  }

  //SchemaRelationProvider要实现的方法
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
    val path: Option[String] = parameters.get("path")
    path match {
      case Some(p) => new CustomUploadDataSourceRelation(sqlContext,p,schema)
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
      case SaveMode.Append => sys.error("Append mode is not supported by " + this.getClass.getCanonicalName); sys.exit(1)
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

    for (elem <- parameters) {
      logError(f"key is ${elem._1}.........value is ${elem._2}")
    }

    data.select($"*",$"domain".as("partitionDomain"),$"time".as("partitionTime"))
      .coalesce(1)
      .write
      .partitionBy("partitionDomain","partitionTime")
      .options(parameters)
      .mode(mode)
      .format("csv")
      .save(path)
  }


}
