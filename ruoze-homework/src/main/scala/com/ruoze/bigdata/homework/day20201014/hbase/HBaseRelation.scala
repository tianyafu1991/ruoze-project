package com.ruoze.bigdata.homework.day20201014.hbase

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{HTable, Put, Result}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.sql.sources.{BaseRelation, InsertableRelation, TableScan}
import org.apache.spark.sql.types.{DateType, DoubleType, FloatType, IntegerType, LongType, StringType, StructField, StructType}

case class HBaseRelation(@transient val sqlContext: SQLContext, parameters: Map[String, String], userSchema: Option[StructType])
  extends BaseRelation
    with TableScan
    with InsertableRelation
    with Logging {

  //TableScan
  override def buildScan(): RDD[Row] = {
    null
  }

  override def schema: StructType = {
    userSchema match {
      case Some(schema) => schema
      case None => {
        null
      }
    }
  }

  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    val configuration: Configuration = getInsertHBaseConfiguration
    val jobConf = new JobConf(configuration, this.getClass)
    jobConf.set(TableOutputFormat.OUTPUT_TABLE, getOutputTableName)
    //获取一个job
    val job = Job.getInstance(jobConf)
    job.setOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setOutputValueClass(classOf[Result])
    job.setOutputFormatClass(classOf[TableOutputFormat[ImmutableBytesWritable]])

    val schema: StructType = data.schema
    val rowkey = getRowKeyFieldName(schema)
    val fields = schema.toArray
    val rowkeyField: (StructField, Int) = fields.zipWithIndex.filter(f => f._1.name == rowkey).head
    val columnFields: Array[(StructField, Int)] = fields.zipWithIndex.filter(f => f._1.name != rowkey)

    data.rdd.map(row => convertRow2Put(row,rowkeyField,columnFields)).saveAsNewAPIHadoopDataset(job.getConfiguration)
  }

  def getZkUrl: String = parameters.getOrElse("hbase.zookeeper.quorum", "hadoop:2181")

  def getOutputTableName: String = parameters.getOrElse("hbase.table", sys.error("HBase表名不能为空....."))

  def getRowKeyFieldName(schema: StructType): String = parameters.getOrElse("hbase.table.rowkey.filed.name", schema.head.name)

  def getInsertHBaseConfiguration(): Configuration = {
    val configuration: Configuration = HBaseConfiguration.create()
    configuration.set("hbase.zookeeper.quorum", getZkUrl)
    configuration.set(TableOutputFormat.OUTPUT_TABLE, getOutputTableName)
    configuration
  }

  def getHBaseColumnFamily(): String = {
    parameters.getOrElse("hbase.table.column.family", "f")
  }

  def generateHBaseRowKey(rowKeyIndex: (StructField, Int), row: Row): HBaseRowKey = {
    rowKeyIndex._1.dataType match {
      case StringType => HBaseRowKey(Bytes.toBytes(row.getString(rowKeyIndex._2)))
      case IntegerType => HBaseRowKey(Bytes.toBytes(row.getInt(rowKeyIndex._2)))
      case FloatType => HBaseRowKey(Bytes.toBytes(row.getFloat(rowKeyIndex._2)))
      case LongType => HBaseRowKey(Bytes.toBytes(row.getLong(rowKeyIndex._2)))
      case DoubleType => HBaseRowKey(Bytes.toBytes(row.getDouble(rowKeyIndex._2)))
      case _ => HBaseRowKey(Bytes.toBytes(row.getString(rowKeyIndex._2)))
    }
  }

  def generateHBaseColumn(columnFields: (StructField, Int), row: Row): HBaseColumn = {
    columnFields._1.dataType match {
      case StringType => if (row.getString(columnFields._2) == null) {
        HBaseColumn(Bytes.toBytes(columnFields._1.name), Bytes.toBytes(""))
      } else {
        HBaseColumn(Bytes.toBytes(columnFields._1.name), Bytes.toBytes(row.getString(columnFields._2)))
      }
      case IntegerType => if (row.getInt(columnFields._2) == null) {
        HBaseColumn(Bytes.toBytes(columnFields._1.name), Bytes.toBytes(0))
      } else {
        HBaseColumn(Bytes.toBytes(columnFields._1.name), Bytes.toBytes(row.getInt(columnFields._2)))
      }
      case FloatType => if (row.getFloat(columnFields._2) == null) {
        HBaseColumn(Bytes.toBytes(columnFields._1.name), Bytes.toBytes(0f))
      } else {
        HBaseColumn(Bytes.toBytes(columnFields._1.name), Bytes.toBytes(row.getFloat(columnFields._2)))
      }
      case LongType => if (row.getLong(columnFields._2) == null) {
        HBaseColumn(Bytes.toBytes(columnFields._1.name), Bytes.toBytes(0L))
      } else {
        HBaseColumn(Bytes.toBytes(columnFields._1.name), Bytes.toBytes(row.getLong(columnFields._2)))
      }
      case DoubleType => if (row.getDouble(columnFields._2) == null) {
        HBaseColumn(Bytes.toBytes(columnFields._1.name), Bytes.toBytes(0D))
      } else {
        HBaseColumn(Bytes.toBytes(columnFields._1.name), Bytes.toBytes(row.getDouble(columnFields._2)))
      }
    }
  }

  def convertRow2Put(row: Row, rowkeyField: (StructField, Int), columnFields: Array[(StructField, Int)]): (ImmutableBytesWritable,Put) = {
    val put = new Put(generateHBaseRowKey(rowkeyField, row).rowKey)
    val hBaseColumns: Array[HBaseColumn] = columnFields.map(generateHBaseColumn(_, row))
    val family: String = getHBaseColumnFamily
    for (hbaseColumn <- hBaseColumns) {
      put.addColumn(Bytes.toBytes(family), hbaseColumn.columnName, hbaseColumn.columnValue)
    }
    (new ImmutableBytesWritable,put)
  }

  //使用kryo将Put进行注册之后，就不用将convertRow2Put转换为函数了，如果不注册，可以使用函数来避免未序列化报错
  val generatePut : (Row, (StructField, Int),Array[(StructField, Int)]) =>(ImmutableBytesWritable,Put)  = convertRow2Put _

}

case class HBaseRowKey(rowKey: Array[Byte])

case class HBaseColumn(columnName: Array[Byte], columnValue: Array[Byte])
