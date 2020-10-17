package com.ruoze.bigdata.homework.day20200929.v1.upload2HDFS

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.StructType

class DefaultSource extends RelationProvider with SchemaRelationProvider with CreatableRelationProvider{

  //RelationProvider要实现的方法
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext,parameters,null)
  }

  //SchemaRelationProvider要实现的方法
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
    val path: Option[String] = parameters.get("path")
    path match {
      case Some(p) => new UploadDataSourceRelation(sqlContext,p,schema)
      case _ => throw new IllegalArgumentException("path is not exists....")
    }
  }

  //CreatableRelationProvider
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
    val path: String = parameters.getOrElse("path", "")
    val fsPath = new Path(path)
    val fs: FileSystem = fsPath.getFileSystem(sqlContext.sparkContext.hadoopConfiguration)

    mode match {
      case SaveMode.Append => println("append......")
      case SaveMode.Overwrite => fs.delete(fsPath,true)
      case SaveMode.ErrorIfExists => sys.error("Given path: " + path + " already exists!!"); sys.exit(1)
      case SaveMode.Ignore => sys.exit()
    }

    val formatName = parameters.getOrElse("format", "customFormat")

    formatName match {
      case "customFormat" => saveAsCustomFormat(data, path, mode)
      case "json" => saveAsJson(data, path, mode)
      case _ => throw new IllegalArgumentException(formatName + " is not supported!!!")
    }
    createRelation(sqlContext,parameters,data.schema)
  }

  private def saveAsJson(data : DataFrame, path : String, mode: SaveMode): Unit = {
    /**
     * Here, I am using the dataframe's Api for storing it as json.
     * you can have your own apis and ways for saving!!
     */
    data.write.mode(mode).json(path)
  }

  private def saveAsCustomFormat(data : DataFrame, path : String, mode: SaveMode): Unit = {
    /**
     * Here, I am  going to save this as simple text file which has values separated by "|".
     * But you can have your own way to store without any restriction.
     */

    import data.sparkSession.implicits._

    //域名为ruozedata.com的，取 domain,city,isp三个字段
    data.filter('domain === "ruozedata.com").coalesce(1)
      .select($"domain".as("my_domain"),$"city",$"isp",$"domain",$"time")
      .coalesce(1)
      .write
      .partitionBy("domain","time")
      .option("sep","$$$")
      .option("format" , "customFormat")
      .mode(mode)
      .format("csv")
      .save(path)

    //域名为ruoze.ke.qq.com的，取 domain,city,isp三个字段
    data.filter('domain === "ruoze.ke.qq.com")
      .select($"domain".as("my_domain"),$"time".as("my_time"),$"cache",$"domain",$"time")
      .coalesce(1)
      .write
      .partitionBy("domain","time")
      .option("sep","\t")
      .option("format" , "customFormat")
      .mode(mode)
      .format("csv")
      .save(path)
  }
}
