package com.ruoze.spark.streaming.day20200923

import com.ruoze.spark.streaming.utils.{ConnectionPool, MySQLUtils}
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object ForeachRDDApp extends Logging {


  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(conf, Seconds(5))

    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop01", 9527)
    val result: DStream[(String, Long)] = lines.flatMap(_.split(",")).countByValue()
    save2MySQL06(result)


    ssc.start()
    ssc.awaitTermination()
  }


  /**
   * 最优的一种方式，使用了ConnectionPool+prepareStatement+batch+batch控制数据量的方式
   * @param result
   */
  def save2MySQL06(result: DStream[(String, Long)]): Unit = {
    result.foreachRDD(
      rdd => {
        if (!rdd.isEmpty()) {

          rdd.foreachPartition(partition => {
            val connection = ConnectionPool.getConnection2()
            logError(s"..........${connection}")
            //关闭自动提交
            connection.setAutoCommit(false)
            val sql = s"insert into wc(word,cnt) values (?,?)"
            val statement = connection.prepareStatement(sql)
            val index: Iterator[((String, Long), Int)] = partition.zipWithIndex
            index.foreach {
              case ((word, cnt),index) => {
                statement.setString(1, word)
                statement.setLong(2, cnt)
                statement.addBatch()
                if(0 != index && index % 5 ==0){
                  statement.executeBatch()
                  //提交
                  connection.commit()
                }
              }
            }
            statement.executeBatch()
            //提交
            connection.commit()
            statement.close()
            ConnectionPool.returnConnection2(connection)
          })
        }
      }
    )
  }



  /**
   * 批次执行sql，如果一个批次的数据量很大 还是会有问题
   * @param result
   */
  def save2MySQL05(result: DStream[(String, Long)]): Unit = {
    result.foreachRDD(
      rdd => {
        if (!rdd.isEmpty()) {

          rdd.foreachPartition(partition => {
            val connection = ConnectionPool.getConnection2()
            logError(s"..........${connection}")
            val sql = s"insert into wc(word,cnt) values (?,?)"
            val statement = connection.prepareStatement(sql)
            partition.foreach {
              case (word, cnt) => {
                statement.setString(1, word)
                statement.setLong(2, cnt)
                statement.addBatch()
              }
            }
            statement.executeBatch()
            statement.close()
            ConnectionPool.returnConnection2(connection)
          })
        }
      }
    )
  }


  /**
   * 使用连接池 创建连接 还连接
   *
   * @param result
   */
  def save2MySQL04(result: DStream[(String, Long)]): Unit = {
    result.foreachRDD(
      rdd => {
        if (!rdd.isEmpty()) {

          rdd.foreachPartition(partition => {
            val connection = ConnectionPool.getConnection2()
            logError(s"..........${connection}")
            val sql = s"insert into wc(word,cnt) values (?,?)"
            val statement = connection.prepareStatement(sql)
            partition.foreach(pair => {
              statement.setString(1, pair._1)
              statement.setLong(2, pair._2)
              statement.execute()
            })
            ConnectionPool.returnConnection2(connection)
          })
        }
      }
    )
  }


  /**
   * 这个是一个partition创建一个连接，性能较好，但这里用了connection.createStatement()，没有用connection.prepareStatement()
   * 还是有性能问题的
   *
   * @param result
   */
  def save2MySQL03(result: DStream[(String, Long)]): Unit = {
    result.foreachRDD(
      rdd => {
        if (!rdd.isEmpty()) {

          rdd.foreachPartition(partition => {
            val connection = MySQLUtils.getConnection()
            logError(s"..........${connection}")
            partition.foreach(pair => {
              val sql = s"insert into wc(word,cnt) values ('${pair._1}',${pair._2})"
              connection.createStatement().execute(sql)
            })
            MySQLUtils.close(connection)
          })
        }
      }
    )
  }


  /**
   * 一个key创建一个连接，性能极差，可能造成连接耗尽
   *
   * @param result
   */
  def save2MySQL02(result: DStream[(String, Long)]): Unit = {
    result.foreachRDD(
      rdd => {
        if (!rdd.isEmpty()) {
          rdd.foreach(
            pair => {
              val connection = MySQLUtils.getConnection()
              logError(s"..........${connection}.....${pair._1}........${pair._2}")
              val sql = s"insert into wc(word,cnt) values ('${pair._1}',${pair._2})"
              connection.createStatement().execute(sql)
              MySQLUtils.close(connection)
            }
          )
        }
      }
    )
  }

  /**
   * 这个是错误的例子，会报错 connection不能序列化
   *
   * @param result
   */
  def save2MySQL(result: DStream[(String, Long)]): Unit = {
    result.foreachRDD(
      rdd => {
        if (!rdd.isEmpty()) {
          val connection = MySQLUtils.getConnection()
          logError(s"..........${connection}")
          rdd.foreach(
            pair => {
              val sql = s"insert into wc(word,cnt) values ('${pair._1}',${pair._2})"
              connection.createStatement().execute(sql)
            }
          )
          MySQLUtils.close(connection)
        }
      }

    )
  }

}
