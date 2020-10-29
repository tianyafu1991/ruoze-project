package com.ruoze.bigdata.homework.day20201014.hbase

import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, DataSourceRegister, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

class DefaultSource extends RelationProvider with SchemaRelationProvider with CreatableRelationProvider with DataSourceRegister {
  //RelationProvider
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    HBaseRelation(sqlContext, parameters, None)
  }

  //SchemaRelationProvider
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
    HBaseRelation(sqlContext, parameters, Some(schema))
  }

  //CreatableRelationProvider
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
    val relation = HBaseRelation(sqlContext, parameters, Some(data.schema))
    relation.insert(data, false)
    relation
  }

  //DataSourceRegister
  override def shortName(): String = "hbase"




}
