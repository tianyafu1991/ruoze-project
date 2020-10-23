package com.ruoze.bigdata.tututuhomework.day20201019.accessEtl.utils

import com.ruoze.bigdata.tututuhomework.day20201019.demo.hbase.SparkSchema
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{HTable, Put}
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

import scala.util.Random

object HBaseUtils {
  private val random: Random = new Random(1000)

  def getHBaseConfiguration(quorum: String, hbaseTable: String): Configuration = {
    val configuration: Configuration = HBaseConfiguration.create()
    configuration.set("hbase.zookeeper.quorum", quorum)
    configuration.set(TableOutputFormat.OUTPUT_TABLE, hbaseTable)
    configuration
  }

  def getHTable(conf: Configuration, tableName: String):HTable ={
    new HTable(conf, tableName)
  }

  def castRow2Put(schema: StructType, row: Row): Put = {
    val put = new Put(generateRowKey(row).getBytes())
    schema.fields.map(field => {
      val fieldName = field.name
      val data = row.getAs[String](fieldName)
      put.addColumn("f".getBytes(), fieldName.getBytes(), data.getBytes())
    })
    put
  }

  def generateRowKey(row: Row): String = {
    val second = row.getAs[String]("second")
    val time = System.currentTimeMillis()
    val randomInt: Int = random.nextInt()
    f"row_key_${second}_${time}_${randomInt}"
  }

  /**
   * schema (age int,name string,sex string)
   * @param schema
   * @return
   */
  def extractSparkFields(schema:String):Array[SparkSchema]={
    schema.trim.drop(1).dropRight(1).split(",").map(column => {
      val splits: Array[String] = column.trim.split(" ")
      SparkSchema(splits(0),splits(1))
    })
  }


}
