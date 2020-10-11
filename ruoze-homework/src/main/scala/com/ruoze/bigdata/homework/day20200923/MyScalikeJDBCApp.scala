package com.ruoze.bigdata.homework.day20200923

import java.sql.Connection

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import scalikejdbc.{ConnectionPool, DB, SQL, scalikejdbcSQLInterpolationImplicitDef}
import scalikejdbc.config.DBs


object MyScalikeJDBCApp extends Logging {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(conf, Seconds(5))

    DBs.setupAll()
    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop01", 9527)
    val result: DStream[(String, Long)] = lines.flatMap(_.split(",")).countByValue()
    save2MySQL07(result)


    ssc.start()
    ssc.awaitTermination()
  }

  /**
   * 使用scalikejdbc批量写入到MySQL中，控制parpareStatement的批的数据量
   * @param result
   */
  def save2MySQL07(result: DStream[(String, Long)]): Unit = {
    result.foreachRDD(
      rdd => {
        if (!rdd.isEmpty()) {
          rdd.foreachPartition(partition => {
            val conn: Connection = ConnectionPool.borrow()
            logError(s"...............${conn}..............")
            val index: Iterator[((String, Long), Int)] = partition.zipWithIndex

            var batchInsertParams: List[List[Any]] = Nil
            val db: DB = DB(conn)

            db.using(conn) {
              con: java.sql.Connection => {
                // set as auto-close disabled
                db.autoClose(false)

                index.foreach {
                  case ((word, cnt), index) => {
                    batchInsertParams = batchInsertParams :+ List(word, cnt)
                    if (0 != index && index % 5 == 0) {
                      db.autoCommit { implicit session =>
                        sql"insert into wc(word,cnt) values (?,?)".batch(batchInsertParams: _*).apply()
                        logError(s"插入数据，数据条数为${batchInsertParams.size},连接为${conn}")
                      }
                      batchInsertParams = Nil
                    }
                  }
                }
                db.autoCommit { implicit session =>
                  sql"insert into wc(word,cnt) values (?,?)".batch(batchInsertParams: _*).apply()
                  logError(s"foreach算子之后插入数据，数据条数为${batchInsertParams.size},连接为${conn}")
                  batchInsertParams = Nil
                }
              }
            }

            //关闭连接
            conn.close()

          })
        }
      }
    )
  }
}


