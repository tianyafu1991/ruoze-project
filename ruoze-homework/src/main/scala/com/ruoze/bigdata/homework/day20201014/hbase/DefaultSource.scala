package com.ruoze.bigdata.homework.day20201014.hbase

import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, DataSourceRegister, RelationProvider}

class DefaultSource extends RelationProvider with CreatableRelationProvider with DataSourceRegister{
  //RelationProvider
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    null
  }

  //CreatableRelationProvider
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
    val relation = new HBaseRelation(sqlContext, parameters, Some(data.schema))
    relation.insert(data,false)
    relation
  }

  //DataSourceRegister
  override def shortName(): String = "hbase"
}
