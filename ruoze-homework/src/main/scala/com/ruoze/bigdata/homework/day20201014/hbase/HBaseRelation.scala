package com.ruoze.bigdata.homework.day20201014.hbase

import java.{lang, util}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{Cell, CellUtil, HBaseConfiguration}
import org.apache.hadoop.hbase.client.{HTable, Put, Result}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{TableInputFormat, TableOutputFormat}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.sql.sources.{BaseRelation, InsertableRelation, TableScan}
import org.apache.spark.sql.types.{DateType, DoubleType, FloatType, IntegerType, LongType, StringType, StructField, StructType}

import scala.collection.mutable.ArrayBuffer

case class HBaseRelation(@transient val sqlContext: SQLContext, parameters: Map[String, String], userSchema: Option[StructType])
  extends BaseRelation
    with TableScan
    with InsertableRelation
    with Logging {

  override def schema: StructType = {
    getSparkTableSchema match {
      case Some(sparkSchema) => {
        val schemas: Array[SparkSchema] = extractSparkFields(sparkSchema)
        val row: Array[StructField] = schemas.map(field => {
          val structField = field.fieldType.toLowerCase match {
            case "string" => StructField(field.fieldName, StringType)
            case "int" => StructField(field.fieldName, IntegerType)
          }
          structField
        })
        StructType(row)
      }
      case None => StructType(Array(
        StructField("rowKey", StringType),
        StructField("content", StringType)
      ))
    }
  }

  //TableScan
  override def buildScan(): RDD[Row] = {
    val configuration: Configuration = getReadHBaseConfiguration
    val hbaseRDD: RDD[(ImmutableBytesWritable, Result)] = sqlContext.sparkContext.newAPIHadoopRDD(configuration,
      classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result])

    hbaseRDD.foreach(println)
    val columnFamily: String = getHBaseColumnFamily
    val rowKeyColumn: String = getRowKeyForRead
    val resultRDD: RDD[Row] = hbaseRDD.map(_._2).map(result => {
      getSparkTableSchema match {
        case Some(sparkSchema) => {
          val schemas: Array[SparkSchema] = extractSparkFields(sparkSchema)
          val buffer = new ArrayBuffer[Any]()
          schemas.foreach(
            t => {
              t.fieldType.toLowerCase match {
                case "string" => {
                  val fieldName: String = t.fieldName
                  var tmp: Array[Byte] = if ("rowKey" == fieldName || rowKeyColumn == fieldName) result.getRow else result.getValue(Bytes.toBytes(columnFamily), Bytes.toBytes(fieldName))
                  if (null == tmp) {
                    tmp = Bytes.toBytes("")
                  }
                  buffer += new String(tmp)
                }
                case "int" => {
                  val fieldName: String = t.fieldName
                  var tmp: Array[Byte] = if ("rowKey" == fieldName || rowKeyColumn == fieldName) result.getRow else result.getValue(Bytes.toBytes(columnFamily), Bytes.toBytes(fieldName))
                  if (null == tmp) {
                    tmp = Bytes.toBytes(0)
                  }
                  buffer += Integer.valueOf(new String(tmp))
                }
              }
            }
          )
          Row.fromSeq(buffer)
        }
        case None => {
          //将所有的hbase的字段拼成一个大json
          val buffer = new ArrayBuffer[Any]()
          val rowKey = new String(result.getRow)
          buffer += rowKey
          val cells: util.List[Cell] = result.listCells()
          val cellArray: Array[Cell] = new Array[Cell](cells.size())
          val cells1: Array[Cell] = cells.toArray(cellArray)
          for (elem <- cells1) {
            val family = new String(CellUtil.cloneFamily(elem))
            val qualifier = new String(CellUtil.cloneQualifier(elem))
            val value = new String(CellUtil.cloneValue(elem))
            buffer += HBaseCell(family,qualifier,value).toString
          }

          Row.fromSeq(buffer)
        }
      }
    })
    resultRDD
  }

  def getSparkTableSchema(): Option[String] = parameters.get("spark.table.schema")


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

    data.rdd.map(row => convertRow2Put(row, rowkeyField, columnFields)).saveAsNewAPIHadoopDataset(job.getConfiguration)
  }

  /**
   * 抽取出用户定义的hbase table  schema
   *
   * @param schema
   * @return
   */
  def extractSparkFields(schema: String): Array[SparkSchema] = {
    schema.trim.drop(1).dropRight(1).split(",").map(column => {
      val splits: Array[String] = column.trim.split(" ")
      SparkSchema(splits(0), splits(1))
    })
  }

  def getZkUrl: String = parameters.getOrElse("hbase.zookeeper.quorum", "hadoop:2181")

  def getOutputTableName: String = parameters.getOrElse("hbase.out.table", sys.error("HBase表名不能为空....."))

  def getRowKeyForRead(): String = parameters.getOrElse("hbase.table.rowkey.filed.name", sys.error("读HBase表时要指定"))

  def getRowKeyFieldName(schema: StructType): String = parameters.getOrElse("hbase.table.rowkey.filed.name", schema.head.name)

  def getHBaseColumnFamily(): String = parameters.getOrElse("hbase.table.column.family", "f")

  def getInputTableName: String = parameters.getOrElse("hbase.in.table", sys.error("HBase表名不能为空....."))

  /**
   * 获取到HBase写入数据的Configuration
   *
   * @return
   */
  def getReadHBaseConfiguration(): Configuration = {
    val configuration: Configuration = HBaseConfiguration.create()
    configuration.set("hbase.zookeeper.quorum", getZkUrl)
    configuration.set(TableInputFormat.INPUT_TABLE, getInputTableName)
    configuration
  }

  /**
   * 获取HBase写入数据的Configuration
   *
   * @return
   */
  def getInsertHBaseConfiguration(): Configuration = {
    val configuration: Configuration = HBaseConfiguration.create()
    configuration.set("hbase.zookeeper.quorum", getZkUrl)
    configuration.set(TableOutputFormat.OUTPUT_TABLE, getOutputTableName)
    configuration
  }

  /**
   * 获取HBase rowKey 的值
   *
   * @param rowKeyIndex
   * @param row
   * @return
   */
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

  /**
   * 获取HBase Column字段名和字段值
   *
   * @param columnFields
   * @param row
   * @return
   */
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

  /**
   * 将row 根据传入的rowKeyField和columnFileds转换成Put
   *
   * @param row
   * @param rowkeyField
   * @param columnFields
   * @return
   */
  def convertRow2Put(row: Row, rowkeyField: (StructField, Int), columnFields: Array[(StructField, Int)]): (ImmutableBytesWritable, Put) = {
    val put = new Put(generateHBaseRowKey(rowkeyField, row).rowKey)
    val hBaseColumns: Array[HBaseColumn] = columnFields.map(generateHBaseColumn(_, row))
    val family: String = getHBaseColumnFamily
    for (hbaseColumn <- hBaseColumns) {
      put.addColumn(Bytes.toBytes(family), hbaseColumn.columnName, hbaseColumn.columnValue)
    }
    (new ImmutableBytesWritable, put)
  }

  //使用kryo将Put进行注册之后，就不用将convertRow2Put转换为函数了，如果不注册，可以使用函数来避免未序列化报错
  val generatePut: (Row, (StructField, Int), Array[(StructField, Int)]) => (ImmutableBytesWritable, Put) = convertRow2Put _

}

case class HBaseRowKey(rowKey: Array[Byte])

case class HBaseColumn(columnName: Array[Byte], columnValue: Array[Byte])

case class SparkSchema(fieldName: String, fieldType: String)

case class HBaseCell(family:String,qualifier:String,value:String){
  override def toString: String = f"column=${family}:${qualifier},value=${value}"
}