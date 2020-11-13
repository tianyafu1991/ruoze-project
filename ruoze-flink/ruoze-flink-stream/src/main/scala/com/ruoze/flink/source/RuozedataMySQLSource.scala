package com.ruoze.flink.source

import java.sql.{Connection, PreparedStatement}

import com.ruoze.flink.bean.Domain.Student
import com.ruoze.flink.utils.MySQLUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}

/**
 * @author PK哥
 **/
class RuozedataMySQLSource  extends RichSourceFunction[Student]{

  var connection:Connection = _
  var pstmt:PreparedStatement = _

  override def open(parameters: Configuration): Unit = {
    connection = MySQLUtils.getConnection()
    pstmt = connection.prepareStatement("select * from student")
  }

  override def close(): Unit = {
    MySQLUtils.closeConnection(connection)
  }

  /**
   * 处理业务逻辑
   */
  override def run(ctx: SourceFunction.SourceContext[Student]): Unit = {
    val rs = pstmt.executeQuery()
    while(rs.next()) {
      val student = Student(rs.getInt("id"), rs.getString("name"), rs.getInt("age"))
      ctx.collect(student)
    }
  }

  override def cancel(): Unit = ???
}
