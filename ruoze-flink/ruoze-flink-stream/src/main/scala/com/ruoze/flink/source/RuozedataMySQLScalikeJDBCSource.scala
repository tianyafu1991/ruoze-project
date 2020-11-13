package com.ruoze.flink.source

import java.sql.{Connection, PreparedStatement}

import com.ruoze.flink.bean.Domain.Student
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichParallelSourceFunction, RichSourceFunction, SourceFunction}
import scalikejdbc.{DB, SQL}
import scalikejdbc.config.DBs

/**
 * @author PK哥
 **/
class RuozedataMySQLScalikeJDBCSource extends RichParallelSourceFunction[Student]{
  /**
   * 处理业务逻辑
   */
  override def run(ctx: SourceFunction.SourceContext[Student]): Unit = {
    DBs.setupAll()
    DB.readOnly{ implicit  session => {
      SQL("SELECT * FROM student").map(rs => {
        val student = Student(rs.int("id"), rs.string("name"), rs.int("age"))
        ctx.collect(student)
      }).list().apply()
    }
    }

  }

  override def cancel(): Unit = ???
}
