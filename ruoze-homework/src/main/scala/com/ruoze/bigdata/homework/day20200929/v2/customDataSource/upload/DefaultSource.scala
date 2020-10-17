package com.ruoze.bigdata.homework.day20200929.v2.customDataSource.upload

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
      case Some(p) => new CustomUploadDataSourceRelation(sqlContext,p,schema)
      case _ => throw new IllegalArgumentException("path is not exists....")
    }
  }


  //CreatableRelationProvider要实现的方法
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
    null
  }


}
