package com.ruoze.bigdata.tututuhomework.day20201019.accessEtl.hbase

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, RelationProvider}
import org.apache.spark.sql.types.StructType

class DefaultSource extends RelationProvider with CreatableRelationProvider{

  //RelationProvider
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    null
  }

  //CreatableRelationProvider
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {

    val hbaseTable = parameters.getOrElse("hbase.table",sys.error("HBase表名不能为空....."))



    data.coalesce(1).rdd.foreachPartition( partition => {
      val configuration: Configuration = HBaseConfiguration.create()
      configuration.set("hbase.zookeeper.quorum","hadoop:2181")
      configuration.set(TableOutputFormat.OUTPUT_TABLE,hbaseTable)

      partition.foreach(row => {
        val put: Put = cast2Put(data.schema, row)
        put
      })


    })



    null
  }

  def cast2Put(schema: StructType,row:Row): Put ={
    null
  }

  def generateRowKey():String = {
    null
  }
}
