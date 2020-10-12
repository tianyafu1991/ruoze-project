package com.ruoze.spark.streaming.utils

import java.sql.{Connection, DriverManager}

object MySQLUtils {


  def getConnection():Connection = {
    Class.forName("com.mysql.jdbc.Driver")
    val url = "jdbc:mysql://hadoop01:3306/ruozedata?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8"
    val username = "root"
    val password = "root"
    DriverManager.getConnection(url,username,password)
  }


  def close(connection: Connection): Unit ={
    if(null != connection){
      connection.close()
    }
  }



}
