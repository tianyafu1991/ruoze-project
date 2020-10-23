package com.ruoze.bigdata.tututuhomework.day20201019.demo.hbase

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.sources.{BaseRelation, RelationProvider}

class DefaultSource extends RelationProvider{
  //RelationProvider
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    HBaseRelation(sqlContext,parameters)
  }
}
