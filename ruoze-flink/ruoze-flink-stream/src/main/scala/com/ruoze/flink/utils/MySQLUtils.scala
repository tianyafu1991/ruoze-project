package com.ruoze.flink.utils

import java.sql.{Connection, DriverManager}

/**
 * @author PK哥
 **/
object MySQLUtils {

  def getConnection() = {
    Class.forName("com.mysql.jdbc.Driver")
    DriverManager.getConnection("jdbc:mysql://hadoop01:3306/ruozedata?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8","root","root")
  }

  def closeConnection(connection:Connection): Unit = {
    if(null != connection) {
      connection.close()
    }
  }

}
