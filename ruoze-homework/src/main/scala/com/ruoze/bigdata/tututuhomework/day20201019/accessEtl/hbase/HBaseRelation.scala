package com.ruoze.bigdata.tututuhomework.day20201019.accessEtl.hbase

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{HTable, Put}
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.sql.sources.{BaseRelation, InsertableRelation}
import org.apache.spark.sql.types.StructType

import scala.jdk.CollectionConverters.seqAsJavaList

case class HBaseRelation(@transient val sqlContext:SQLContext,parameters: Map[String, String],dataSchema:StructType)
extends BaseRelation with InsertableRelation{

  val hbaseTable = parameters.getOrElse("hbase.table", sys.error("HBase表名不能为空....."))
  val quorum = parameters.getOrElse("hbase.zookeeper.quorum", "hadoop:2181")



  override def schema: StructType = dataSchema

  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    data.coalesce(1).rdd.foreachPartition(partition => {
      val configuration: Configuration = HBaseConfiguration.create()
      configuration.set("hbase.zookeeper.quorum", quorum)
      configuration.set(TableOutputFormat.OUTPUT_TABLE, hbaseTable)

      val puts: Iterator[Put] = partition.map(cast2Put(data.schema, _))

      val list: List[Put] = puts.toList

      val hTable = new HTable(configuration, hbaseTable)

      hTable.put(seqAsJavaList(list))
      hTable.flushCommits()
    })
  }

  def cast2Put(schema: StructType, row: Row): Put = {
    val put = new Put(generateRowKey(row).getBytes())
    schema.fields.map(field => {
      val fieldName = field.name
      val data = row.getAs[String](fieldName)
      put.addColumn("f".getBytes(),fieldName.getBytes(),data.getBytes())
    })

    put
  }

  def generateRowKey(row: Row): String = {
    val second = row.getAs[String]("second")
    val time = System.currentTimeMillis()
    f"row_key_${second}_${time}"
  }
}
