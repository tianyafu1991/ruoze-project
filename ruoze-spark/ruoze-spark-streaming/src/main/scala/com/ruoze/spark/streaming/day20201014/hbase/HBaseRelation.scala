package com.ruoze.spark.streaming.day20201014.hbase

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.sources.{BaseRelation, TableScan}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

import scala.collection.mutable.ArrayBuffer

case class HBaseRelation(@transient sqlContext: SQLContext, parameters: Map[String, String])
  extends BaseRelation with TableScan with Logging {

  val hbaseTable = parameters.getOrElse("hbase.table",sys.error("HBase表名不能为空....."))
  val sparkTableSchema = parameters.getOrElse("spark.table.schema",sys.error("spark表的schema信息不能为空"))

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
    configuration.set("hbase.zookeeper.quorum","hadoop:2181")
    configuration.set(TableInputFormat.INPUT_TABLE,hbaseTable)

    val hbaseRDD: RDD[(ImmutableBytesWritable, Result)] = sqlContext.sparkContext.newAPIHadoopRDD(configuration,
      classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result])

    hbaseRDD.foreach(println)

    val resultRDD: RDD[Row] = hbaseRDD.map(_._2).map(result => {
      val buffer = new ArrayBuffer[Any]()
      sparkFields.foreach(field => {
        field.fieldType.toLowerCase match {
          case "string" => {
            val tmp = result.getValue(Bytes.toBytes("o"), Bytes.toBytes(field.fieldName))
            if(null != tmp){
              buffer += new String(tmp)
            }
          }
          case "int" => {
            val tmp: Array[Byte] = result.getValue(Bytes.toBytes("o"), Bytes.toBytes(field.fieldName))
            if(null != tmp){
              buffer += Integer.valueOf(new String(tmp))
            }
          }
        }
      })
      Row.fromSeq(buffer)
    })

    resultRDD
  }























}
