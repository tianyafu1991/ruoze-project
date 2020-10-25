package com.ruoze.bigdata.tututuhomework.day20201019_bak.accessEtl.hbase

import org.apache.spark.sql.{DataFrame, Row, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, RelationProvider}

class DefaultSource extends RelationProvider with CreatableRelationProvider {

  //RelationProvider
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    HBaseRelation(sqlContext,parameters,null)
  }

  //CreatableRelationProvider
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
    val relation = HBaseRelation(sqlContext, parameters, data.schema)
    relation.insert(data,false)
    relation
  }
}
