package com.ruoze.bigdata.homework.day20200929.rawETL

import org.apache.hadoop.fs.Path
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.StructType

class DefaultSource extends RelationProvider with SchemaRelationProvider with CreatableRelationProvider {

  //RelationProvider要重写的方法，用来读取数据
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext, parameters, null)
  }

  //SchemaRelationProvider要实现的方法，用来用户传入一个自定义的schema
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
    val path: Option[String] = parameters.get("path")
    path match {
      case Some(p) =>new RawDataSourceRelation(sqlContext,p,schema)
      case _ => throw new IllegalArgumentException("path is not exists....")
    }
  }

  //CreatableRelationProvider要重写的方法，用来写出数据
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
    val path = parameters.getOrElse("path", "./output/") //can throw an exception/error, it's just for this tutorial
    val fsPath = new Path(path)
    val fs = fsPath.getFileSystem(sqlContext.sparkContext.hadoopConfiguration)

    mode match {
      case SaveMode.Append => sys.error("Append mode is not supported by " + this.getClass.getCanonicalName); sys.exit(1)
      case SaveMode.Overwrite => fs.delete(fsPath, true)
      case SaveMode.ErrorIfExists => sys.error("Given path: " + path + " already exists!!"); sys.exit(1)
      case SaveMode.Ignore => sys.exit()
    }

    val formatName = parameters.getOrElse("format", "customFormat")
    formatName match {
      case "customFormat" => saveAsCustomFormat(data, path, mode)
      case "json" => saveAsJson(data, path, mode)
      case _ => throw new IllegalArgumentException(formatName + " is not supported!!!")
    }
    createRelation(sqlContext, parameters, data.schema)
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
    /*val customFormatRDD = data.rdd.map(row => {
      row.toSeq.map(value => value.toString).mkString("|")
    })
    customFormatRDD.saveAsTextFile(path)*/

    import data.sparkSession.implicits._

    data.select($"*",$"time".as("myTime"),$"domain".as("myDomain"))
      .write
      .partitionBy("time","domain")
      .options(Map("format" -> "customFormat"))
      .mode(mode)
      .format("csv")
      .save(path)
  }


}
