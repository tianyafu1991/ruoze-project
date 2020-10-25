package com.ruoze.bigdata.tututuhomework.day20201019.etl.hbase

import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, RelationProvider}

class DefaultSource extends RelationProvider with CreatableRelationProvider with Serializable {

  //RelationProvider
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    HBaseRelation(sqlContext, parameters, null)
  }

  //CreatableRelationProvider
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
    val relation = HBaseRelation(sqlContext, parameters, data.schema)
    relation.insert(data, false)
    relation
  }
}
