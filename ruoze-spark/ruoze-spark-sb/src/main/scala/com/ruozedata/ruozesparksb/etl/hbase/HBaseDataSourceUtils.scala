package com.ruozedata.ruozesparksb.etl.hbase

import com.ruozedata.ruozesparksb.domain.SparkSchema

object HBaseDataSourceUtils {

  /**
   * schema (age int,name string,sex string)
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

  def main(args: Array[String]): Unit = {

    extractSparkFields("(ip string,proxyIp string,responseTime string,referer string,method string,url string,httpCode string,requestSize string,responseSize string,cache string,uaHead string,fileType string,province string,city string,isp string,http string,domain string,path string,params string,year string,month string,day string,hour string,minute string,second string,time string)").foreach(println)
  }

}
