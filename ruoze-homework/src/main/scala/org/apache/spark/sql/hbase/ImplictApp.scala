package org.apache.spark.sql.hbase

import org.apache.spark.sql.{DataFrame, SparkSession}

object ImplictApp {

  implicit def toSparkSqlContextFunctions(spark: SparkSession): SparkSqlContextFunctions = {
    new SparkSqlContextFunctions(spark)
  }

  implicit def toDataFrameFunctions(data: DataFrame): DataFrameFunctions = {
    new DataFrameFunctions(data)
  }

}
