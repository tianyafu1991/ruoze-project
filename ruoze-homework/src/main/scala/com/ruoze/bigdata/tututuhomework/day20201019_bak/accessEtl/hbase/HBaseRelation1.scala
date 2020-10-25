package com.ruoze.bigdata.tututuhomework.day20201019_bak.accessEtl.hbase
/*
import com.ruoze.bigdata.tututuhomework.day20201019.accessEtl.utils.HBaseUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{HTable, Put, Result}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.sql.sources.{BaseRelation, InsertableRelation, TableScan}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.seqAsJavaList

case class HBaseRelation1(@transient val sqlContext: SQLContext, parameters: Map[String, String], dataSchema: StructType)
  extends BaseRelation with TableScan with InsertableRelation with Logging {

  val quorum: String = parameters.getOrElse("hbase.zookeeper.quorum", "hadoop:2181")

  val hbaseTable = parameters.getOrElse("hbase.table",sys.error("HBase表名不能为空....."))
  val sparkTableSchema = parameters.getOrElse("spark.table.schema",sys.error("spark表的schema信息不能为空"))

  private val sparkFields: Array[SparkSchema] = HBaseDataSourceUtils.extractSparkFields(sparkTableSchema)


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

  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    data.coalesce(1).rdd.foreachPartition(partition => {
      val configuration: Configuration = HBaseUtils.getHBaseConfiguration(quorum, hbaseTable)

      val puts: Iterator[Put] = partition.map(x => {
        HBaseUtils.castRow2Put(x.schema, x)
      })

      val list: List[Put] = puts.toList

      val hTable = HBaseUtils.getHTable(configuration, hbaseTable)

      hTable.put(seqAsJavaList(list))
      hTable.flushCommits()
    })
  }

  override def buildScan(): RDD[Row] = {
    val configuration: Configuration = HBaseUtils.getHBaseConfiguration(quorum, hbaseTable)

    val hbaseRDD: RDD[(ImmutableBytesWritable, Result)] = sqlContext.sparkContext.newAPIHadoopRDD(configuration,
      classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result])

    hbaseRDD.foreach(println)

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
}*/
