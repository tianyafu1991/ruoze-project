package com.ruoze.bigdata.tututuhomework.day20201019_bak.accessEtl.hbase

import java.util

import com.ruoze.bigdata.tututuhomework.day20201019_bak.accessEtl.utils.HBaseUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Put, Result}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.sources.{BaseRelation, InsertableRelation, TableScan}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._


case class HBaseRelation(@transient sqlContext: SQLContext, parameters: Map[String, String],userSchema:StructType)
  extends BaseRelation with TableScan with Logging with InsertableRelation{

  val quorum = parameters.getOrElse("hbase.zookeeper.quorum", "hadoop01:2181")
  val hbaseTable = parameters.getOrElse("hbase.table", sys.error("HBase表名不能为空....."))
  val sparkTableSchema = parameters.getOrElse("spark.table.schema", sys.error("spark表的schema信息不能为空"))

  private val sparkFields: Array[SparkSchema] = HBaseDataSourceUtils.extractSparkFields(sparkTableSchema)

  //BaseRelation
  override def schema: StructType = {
    val row: Array[StructField] = sparkFields.map(field => {
      val structField = field.fieldType.toLowerCase match {
        case "string" => StructField(field.fieldName, StringType)
        case "int" => StructField(field.fieldName, IntegerType)
      }
      structField
    })
    new StructType(row)
  }

  //TableScan
  override def buildScan(): RDD[Row] = {
    val configuration: Configuration = HBaseConfiguration.create()
    configuration.set("hbase.zookeeper.quorum","hadoop01:2181")
    configuration.set(TableInputFormat.INPUT_TABLE,"ruozedata:access_log")
//    val  configuration = HBaseUtils.getHBaseConfiguration(quorum,hbaseTable)
    val hbaseRDD: RDD[(ImmutableBytesWritable, Result)] = sqlContext.sparkContext.newAPIHadoopRDD(configuration,
      classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result])

//    hbaseRDD.foreach(println)

    val resultRDD: RDD[Row] = hbaseRDD.map(_._2).map(result => {
      logError(result.toString)
      val buffer = new ArrayBuffer[Any]()
      sparkFields.foreach(field => {
        field.fieldType.toLowerCase match {
          case "string" => {
            val tmp = result.getValue(Bytes.toBytes("f"), Bytes.toBytes(field.fieldName))
            if (null != tmp) {
              buffer += new String(tmp)
            }
          }
          case "int" => {
            val tmp: Array[Byte] = result.getValue(Bytes.toBytes("f"), Bytes.toBytes(field.fieldName))
            if (null != tmp) {
              buffer += Integer.valueOf(new String(tmp))
            }
          }
        }
      })
      Row.fromSeq(buffer)
    })

    resultRDD
  }

  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    data.coalesce(1).rdd.foreachPartition(partition => {
      val configuration: Configuration = HBaseConfiguration.create()
      configuration.set("hbase.zookeeper.quorum","hadoop01:2181")
      configuration.set(TableInputFormat.INPUT_TABLE,hbaseTable)

      val puts: Iterator[Put] = partition.map(x => {
        HBaseUtils.castRow2Put(x.schema, x)
      })

      val putList: util.List[Put] = puts.toList.asJava
//      val list: List[Put] = puts.toList

      val hTable = HBaseUtils.getHTable(configuration, hbaseTable)

      hTable.put(putList)
      hTable.flushCommits()
    })
  }
}
